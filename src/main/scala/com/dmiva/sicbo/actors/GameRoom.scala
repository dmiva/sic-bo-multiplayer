package com.dmiva.sicbo.actors

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{ActorLogging, ActorRef, OneForOneStrategy, Props, Timers}
import akka.persistence.{PersistentActor, SaveSnapshotFailure, SaveSnapshotSuccess, SnapshotOffer}
import com.dmiva.sicbo.actors.GameRoom.Event.GotDiceResult
import com.dmiva.sicbo.actors.repository.{CborSerializable, PlayerRepository}
import com.dmiva.sicbo.common.OutgoingMessage
import com.dmiva.sicbo.common.OutgoingMessage.{BetAccepted, BetRejected, GamePhaseChanged}
import com.dmiva.sicbo.domain.{Balance, Bet, DiceOutcome, GamePhase, GameState, Name, Player}

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object GameRoom {

  sealed trait Command extends CborSerializable
  object Command {
    case class Join(ref: ActorRef, player: Player) extends Command
    case class Leave(ref: ActorRef, player: Player) extends Command
    case class PlaceBet(ref: ActorRef, player: Player, bets: List[Bet]) extends Command
  }

  /** Persisted events */
  sealed trait Event extends CborSerializable
  object Event {
    case class PlayerJoined(player: Player) extends Event
    case class PlayerLeft(player: Player) extends Event
    case class BetPlaced(player: Player, bets: List[Bet]) extends Event
    case class GotDiceResult(dice: DiceOutcome) extends Event
    case class GamePhaseChanged(newPhase: GamePhase) extends Event
  }

  /** Timers for game phase change scheduling */
  sealed abstract case class TimerSetting private (nextPhase: GamePhase, duration: FiniteDuration)
  object TimerSetting {
    val placingBets: TimerSetting = TimerSetting(nextPhase = GamePhase.RollingDice, duration = 15.seconds)
    val rollingDice: TimerSetting = TimerSetting(nextPhase = GamePhase.MakePayouts, duration = 5.seconds)
    val makePayouts: TimerSetting = TimerSetting(nextPhase = GamePhase.GameEnded, duration = 5.seconds)

    private def apply(nextPhase: GamePhase, duration: FiniteDuration): TimerSetting =
      new TimerSetting(nextPhase, duration){}
  }

  def props() = Props(new GameRoom())
}

class GameRoom extends Timers with PersistentActor with ActorLogging {
  import GameRoom._
  override def persistenceId: String = "game-room-id-1"
  val snapshotInterval = 1

  val diceRoller: ActorRef = context.actorOf(DiceRoller.props())

  var state: GameState = GameState.initGame

  // Supervision strategy for the dice roller actor
  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 2, withinTimeRange = 4.seconds) {
      case _: IllegalStateException    => Restart
      case _: Exception                => Escalate
    }

  /** Schedules timer that sends message to itself with next game phase */
  private def startTimer(timer: TimerSetting): Unit = {
    timers.startSingleTimer("timerKey", timer.nextPhase, timer.duration)
  }

  override def receiveRecover: Receive = {
    case evt: Event => updateState(evt)
    case SnapshotOffer(metadata, snapshot: GameState) => state = snapshot
      log.info(s"Snapshot ${metadata.persistenceId} seqId=${metadata.sequenceNr} recovered successfully.")
      // After recovery timer needs to be scheduled again
      snapshot.phase match {
        case GamePhase.PlacingBets =>
          startTimer(TimerSetting.placingBets)
          println(s"Game recovered. You have ${TimerSetting.placingBets.duration} seconds to place bets...")

        case GamePhase.RollingDice =>
          startTimer(TimerSetting.rollingDice)
          if (snapshot.dice.isEmpty) {
            diceRoller ! DiceRoller.RollDice
            println("Game recovered. Dice are rolling...")
          }
          else
            println(s"Game recovered. Dice result is ${snapshot.dice}")

        case GamePhase.MakePayouts =>
          startTimer(TimerSetting.makePayouts)
          println("Game recovered. Making payouts...")

        case GamePhase.Idle        => println("Game recovered. Waiting for players...")
        case _                     => // do nothing
      }
  }

  override def receiveCommand: Receive = handleCommand(Map.empty)

  def handleCommand(users: Map[ActorRef, Player]): Receive = {
    case SaveSnapshotSuccess(metadata) =>
//      log.info(s"Snapshot for ${metadata.persistenceId} seqId=${metadata.sequenceNr} saved successfully.")

    case SaveSnapshotFailure(metadata, reason) =>
      log.error(s"Snapshot for ${metadata.persistenceId} seqId=${metadata.sequenceNr} failure. Reason: ${reason.getMessage}")

    case Command.Join(ref, player)  => persist(Event.PlayerJoined(player))(event => handleEvent(event, users, ref))
    case Command.Leave(ref, player) => persist(Event.PlayerLeft(player))(event => handleEvent(event, users, ref))

    case Command.PlaceBet(ref, player, bets) =>
      state.placeBet(player.username, bets) match {
        case Left(e) => ref ! BetRejected(e)
        case Right(_) => persist(Event.BetPlaced(player, bets))(event => handleEvent(event, users, ref))
      }

    case DiceRoller.DiceResult(dice) => persist(Event.GotDiceResult(dice))(event => handleEvent(event, users, sender()))

    case GamePhase.Idle         => persist(Event.GamePhaseChanged(GamePhase.Idle))(event => handleEvent(event, users, sender()))
    case GamePhase.PlacingBets  => persist(Event.GamePhaseChanged(GamePhase.PlacingBets))(event => handleEvent(event, users, sender()))
    case GamePhase.RollingDice  => persist(Event.GamePhaseChanged(GamePhase.RollingDice))(event => handleEvent(event, users, sender()))
    case GamePhase.MakePayouts  => persist(Event.GamePhaseChanged(GamePhase.MakePayouts))(event => handleEvent(event, users, sender()))
    case GamePhase.GameEnded    => persist(Event.GamePhaseChanged(GamePhase.GameEnded))(event => handleEvent(event, users, sender()))
  }

  def handleEvent(event: Event, users: Map[ActorRef, Player], replyTo: ActorRef): Unit = {
    updateState(event)
    context.system.eventStream.publish(event)

    // After event is successfully persisted and state mutated, perform remaining actions
    event match {
      case Event.PlayerJoined(player) =>
        println(s"${player.username} joined the game.")
        if (state.phase == GamePhase.Idle) self ! GamePhase.PlacingBets
        replyTo ! GamePhaseChanged(state.phase)
        context.become(handleCommand(users + (replyTo -> player)))

      case Event.PlayerLeft(player) =>
        println(s"${player.username} left the game.")
        context.become(handleCommand(users - replyTo))
        saveBalanceToRepository(player.username)

      case Event.BetPlaced(player, bets) =>
        replyTo ! BetAccepted
        bets.foreach(bet => println(s"${player.username} placed bet with type = ${bet.betType} and amount = ${bet.amount}"))

      case GotDiceResult(dice) =>
        println(s"Received dice result: $dice")

      case Event.GamePhaseChanged(newPhase) =>
        newPhase match {
          case GamePhase.Idle =>
            println("Waiting for players...")

          case GamePhase.PlacingBets =>
            startTimer(TimerSetting.placingBets)
            broadcast(users, GamePhaseChanged(state.phase))
            println(s"New game started. You have ${TimerSetting.placingBets.duration} seconds to place bets...")

          case GamePhase.RollingDice =>
            startTimer(TimerSetting.rollingDice)
            broadcast(users,GamePhaseChanged(state.phase))
            println("Dice are rolling...")
            if (state.dice.isEmpty) diceRoller ! DiceRoller.RollDice

          case GamePhase.MakePayouts =>
            startTimer(TimerSetting.makePayouts)
            broadcast(users,GamePhaseChanged(state.phase))
            println("Making payouts...")
            println(state.gameResult)
            state.gameResult.foreachEntry {
              case (playerName, result) =>
                users.find { case (_, player) => player.username == playerName } match {
                  case Some((ref, player)) => ref ! result
                  case None =>
                }
            }
            state.gameResult.foreachEntry {
              case (playerName, _) =>
                saveBalanceToRepository(playerName)
            }

          case GamePhase.GameEnded =>
            println("Game ended.")
            if (users.nonEmpty)
              self ! GamePhase.PlacingBets
            else
              self ! GamePhase.Idle
        }
    }

    if (lastSequenceNr % snapshotInterval == 0 && lastSequenceNr != 0)
      saveSnapshot(state)
  }

  def updateState(event: Event): Unit = {
    event match {
      case Event.PlayerJoined(player)     => state = state.addPlayer(player)
      case Event.PlayerLeft(player)       => state = state.removePlayer(player)
      case Event.BetPlaced(player, bets)  =>
        state.placeBet(player.username, bets) match {
          case Left(e)                    => log.error(e)
          case Right(newState)            => state = newState
        }
      case GotDiceResult(dice) =>
        state.applyDiceOutcome(dice) match {
          case Left(e)              => log.error(e)
          case Right(newState)      => state = newState
        }
      case Event.GamePhaseChanged(newPhase) => newPhase match {
        case GamePhase.Idle         => state = GameState.initGame
        case GamePhase.PlacingBets  => state = state.startNewGame
        case GamePhase.RollingDice  => state = state.setGamePhase(GamePhase.RollingDice)
        case GamePhase.MakePayouts  =>
          state = state.setGamePhase(GamePhase.MakePayouts)
          state.calculateResults match {
            case Left(e) => log.error(e)
            case Right(newState)    => state = newState
          }
        case GamePhase.GameEnded    => // do nothing
      }
    }
  }

  def saveBalanceToRepository(name: Name): Unit = {
    val balance = state.getPlayerBalance(name)
    context.parent ! PlayerRepository.Command.UpdateBalance2(name, Balance(balance))
  }

  def broadcast(players: Map[ActorRef, Player], msg: OutgoingMessage): Unit = {
    players.foreach { case (ref, _) => ref ! msg}
  }

}
