package com.dmiva.sicbo.actors

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{ActorLogging, ActorRef, Kill, OneForOneStrategy, Props, Timers}
import akka.persistence.{PersistentActor, SaveSnapshotFailure, SaveSnapshotSuccess, SnapshotOffer}
import com.dmiva.sicbo.actors.GameRoomPers.Event.GotDiceResult
import com.dmiva.sicbo.actors.repository.{CborSerializable, PlayerRepository}
import com.dmiva.sicbo.common.OutgoingMessage
import com.dmiva.sicbo.common.OutgoingMessage.{BetAccepted, BetRejected, GamePhaseChanged}
import com.dmiva.sicbo.domain.{Balance, Bet, DiceOutcome, GamePhase, GameState, Player}

import scala.concurrent.duration.DurationInt

object GameRoomPers {

  sealed trait Command extends CborSerializable
  object Command {
    case class Join(ref: ActorRef, player: Player) extends Command
    case class Leave(ref: ActorRef, player: Player) extends Command
    case class PlaceBet(ref: ActorRef, player: Player, bets: List[Bet]) extends Command
  }

  sealed trait Event extends CborSerializable
  object Event {
    case class PlayerJoined(player: Player) extends Event
    case class PlayerLeft(player: Player) extends Event
    case class BetPlaced(player: Player, bets: List[Bet]) extends Event
    case class GotDiceResult(dice: DiceOutcome) extends Event
    case class GamePhaseChanged(newPhase: GamePhase) extends Event
  }

  def props() = Props(new GameRoomPers())
}


class GameRoomPers extends Timers with PersistentActor with ActorLogging {
  import GameRoomPers._
  override def persistenceId: String = "game-room-id-1"
  val snapshotInterval = 1

  val diceRoller: ActorRef = context.actorOf(DiceRoller.props())
  var users: Map[ActorRef, Player] = Map()

  var state: GameState = GameState.initGame

  // Supervision strategy for the dice roller actor
  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 2, withinTimeRange = 4.seconds) {
      case _: IllegalStateException    => Restart
      case _: Exception                => Escalate
    }

  override def receiveRecover: Receive = {
    case evt: Event => updateState(evt)
    case SnapshotOffer(metadata, snapshot: GameState) => state = snapshot
      log.info(s"Game snapshot ${metadata.sequenceNr} recovered successfully.")
  }

  def updateState(event: Event): Unit = {
    event match {
      case Event.PlayerJoined(player)     => state = state.addPlayer(player)
      case Event.PlayerLeft(player)       => state = state.removePlayer(player)
      case Event.BetPlaced(player, bets)  =>
        state.placeBet(player.username, bets) match {
          case Left(e)                => log.error(e)
          case Right(newState)            => state = newState
        }
      case GotDiceResult(dice) =>
        state.applyDiceOutcome(dice) match {
          case Left(e) => log.error(e)
          case Right(newState) => state = newState
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

  def handleEvent(event: Event, replyTo: ActorRef): Unit = {
    updateState(event)
    context.system.eventStream.publish(event)

        event match {
          case Event.PlayerJoined(player) => // no action here
          case Event.PlayerLeft(player) => saveBalanceToRepository(player)
          case Event.BetPlaced(player, bets) =>
            replyTo ! BetAccepted
            bets.foreach(bet => println(s"${player.username} placed bet with type = ${bet.betType} and amount = ${bet.amount}"))
          case GotDiceResult(dice) => // no action here
          case Event.GamePhaseChanged(newPhase) =>
            newPhase match {
              case GamePhase.Idle => println("Waiting for players...")
              case GamePhase.PlacingBets =>
                broadcast(users, GamePhaseChanged(state.phase))
                println("New game started. You have 15 seconds to place bets...")
                timers.startSingleTimer("Time before rolling dice", GamePhase.RollingDice, 15.seconds)

              case GamePhase.RollingDice =>
                broadcast(users,GamePhaseChanged(state.phase))
                println("Dice are rolling...")
                diceRoller ! DiceRoller.RollDice
                timers.startSingleTimer("Time before making payouts", GamePhase.MakePayouts, 5.seconds)

              case GamePhase.MakePayouts =>
                broadcast(users,GamePhaseChanged(state.phase))
                println("Making payouts...")
                state.calculateResults match {
                  case Right(newState) => state = newState
                  case Left(e) => log.error("Something bad happened")
                }
                println(state.gameResult)
                // TODO: Send only if Right(newState)
                state.gameResult.foreachEntry {
                  case (playerName, result) =>
                    users.find { case (_, player) => player.username == playerName } match {
                      case Some((ref, session)) => ref ! result
                      case None =>
                    }
                }
                users foreachEntry { case (ref, player) =>
                  saveBalanceToRepository(player)
                }

                timers.startSingleTimer("Time before new round", GamePhase.GameEnded, 5.seconds)

              case GamePhase.GameEnded =>
                println("Game ended.")
                self ! Kill
                if (users.nonEmpty)
                  self ! GamePhase.PlacingBets
                else
                  self ! GamePhase.Idle
            }
        }

    if (lastSequenceNr % snapshotInterval == 0 && lastSequenceNr != 0)
      saveSnapshot(state)
  }



//  override def receiveCommand: Receive = commandHandler(Map())

//  def commandHandler(users: Map[ActorRef, Player]): Receive = {
  override def receiveCommand: Receive = {
    case SaveSnapshotSuccess(metadata)         =>
      log.info(s"Snapshot for ${metadata.persistenceId} seqId=${metadata.sequenceNr} saved successfully.")
    case SaveSnapshotFailure(metadata, reason) =>
      log.error(s"Snapshot for ${metadata.persistenceId} seqId=${metadata.sequenceNr} failure. Reason: ${reason.getMessage}")
    case Command.Join(ref, player) =>
      persist(Event.PlayerJoined(player))(event => handleEvent(event, ref))
      log.info(s"${player.username} joined the game.")
      if (state.phase == GamePhase.Idle) timers.startSingleTimer("Starting game...", GamePhase.PlacingBets, 3.seconds)

//      context.become(commandHandler(users + (ref -> player)))
      users = users + (ref -> player)

    case Command.Leave(ref, player) =>
      log.info(s"${player.username} left the game.")
      persist(Event.PlayerLeft(player))(event => handleEvent(event, ref))
//      context.become(commandHandler(users - ref))
      users = users - ref

    case Command.PlaceBet(ref, player, bets) =>
//      if (state.phase == GamePhase.PlacingBets && users.contains(sender())) {
        state.placeBet(player.username, bets) match {
          case Left(e) => ref ! BetRejected(e)
          case Right(newState) =>
            persist(Event.BetPlaced(player, bets))(event => handleEvent(event, ref))
        }
//      }

    case DiceRoller.DiceResult(dice) => persist(Event.GotDiceResult(dice))(event => handleEvent(event, sender()))

    case GamePhase.Idle         => persist(Event.GamePhaseChanged(GamePhase.Idle))(event => handleEvent(event, sender()))
    case GamePhase.PlacingBets  => persist(Event.GamePhaseChanged(GamePhase.PlacingBets))(event => handleEvent(event, sender()))
    case GamePhase.RollingDice  => persist(Event.GamePhaseChanged(GamePhase.RollingDice))(event => handleEvent(event, sender()))
    case GamePhase.MakePayouts  => persist(Event.GamePhaseChanged(GamePhase.MakePayouts))(event => handleEvent(event, sender()))
    case GamePhase.GameEnded    => persist(Event.GamePhaseChanged(GamePhase.GameEnded))(event => handleEvent(event, sender()))
  }


  def saveBalanceToRepository(player: Player): Unit = {
    val balance = state.getPlayerBalance(player.username)
    context.parent ! PlayerRepository.Command.UpdateBalance(player.username, Balance(balance))
  }

  def broadcast(players: Map[ActorRef, Player], msg: OutgoingMessage): Unit = {
    players.foreach { case (ref, _) => ref ! msg}
  }
}
