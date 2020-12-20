package com.dmiva.sicbo.actors

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props, Timers}
import com.dmiva.sicbo.actors.repository.PlayerRepository
import com.dmiva.sicbo.common.OutgoingMessage.{BetAccepted, BetRejected, Error, GamePhaseChanged}
import com.dmiva.sicbo.common.{BetRejectReason, ErrorMessage, OutgoingMessage}
import com.dmiva.sicbo.domain.{Balance, Bet, GamePhase, GameState, Player}

import scala.concurrent.duration.DurationInt

@deprecated("Replaced with Persistent actor")
object GameRoom {

  case class Join(ref: ActorRef, player: Player)
  case class Leave(ref: ActorRef, player: Player)
  case class PlaceBet(player: Player, bets: List[Bet])

  def props() = Props(new GameRoom)
}

class GameRoom extends Actor with Timers with ActorLogging {
  import GameRoom._

  val diceRoller: ActorRef = context.actorOf(DiceRoller.props())

  var state: GameState = GameState.initGame

  // Supervision strategy for the dice roller actor
  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 2, withinTimeRange = 4.seconds) {
      case _: IllegalStateException    => Restart
      case _: Exception                => Escalate
    }

  override def receive: Receive = playing(Map())

  def playing(users: Map[ActorRef, Player]): Receive = {

    case Join(ref, player) =>
      log.info(s"${player.username} joined the game.")
      if (state.phase == GamePhase.Idle) timers.startSingleTimer("Starting game...", GamePhase.PlacingBets, 3.seconds)
      // persist
//      persist(Event.PlayerJoined(session.player))(event => handleEvent(event, replyTo))
      state = state.addPlayer(player)
      context.become(playing(users + (ref -> player)))

    case Leave(ref, player) =>
      log.info(s"${player.username} left the game.")
      // persist
      state = state.removePlayer(player)
      saveBalanceToRepository(player)
      context.become(playing(users - ref))

    case PlaceBet(player, bets) =>
      if (state.phase == GamePhase.PlacingBets && users.contains(sender())) {
        state.placeBet(player.username, bets) match {
          case Left(e) => sender() ! BetRejected(BetRejectReason.NotEnoughBalance)
          case Right(newState) =>
            // persist
            state = newState
            sender() ! BetAccepted
            bets.map(bet => println(s"${player.username} placed bet with type = ${bet.betType} and amount = ${bet.amount}"))
        }
      }
      else
        if (users.contains(sender())) sender() ! BetRejected(BetRejectReason.TimeExpired)
        else sender() ! Error(ErrorMessage.NotLoggedIn)


    case DiceRoller.DiceResult(dice) =>
      log.info(s"Received dice gameResult $dice")
      // persist
      state.applyDiceOutcome(dice) match {
        case Right(newState) => state = newState
        case Left(e) => log.error("Fatal error")
      }

    case GamePhase.Idle =>
      // persist
      state = GameState.initGame
      println("Waiting for players...")

    case GamePhase.PlacingBets =>
      state = state.startNewGame
      broadcast(users,GamePhaseChanged(state.phase))

      println("New game started. You have 15 seconds to place bets...")
      timers.startSingleTimer("Time before rolling dice", GamePhase.RollingDice, 15.seconds)

    case GamePhase.RollingDice =>
      // persist
      state = state.setGamePhase(GamePhase.RollingDice)
      broadcast(users,GamePhaseChanged(state.phase))

      println("Dice are rolling...")
      diceRoller ! DiceRoller.RollDice
      timers.startSingleTimer("Time before making payouts", GamePhase.MakePayouts, 5.seconds)

    case GamePhase.MakePayouts =>
      // persist
      state = state.setGamePhase(GamePhase.MakePayouts)
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
      if (users.nonEmpty)
        self ! GamePhase.PlacingBets
      else
        self ! GamePhase.Idle
  }

  def saveBalanceToRepository(player: Player): Unit = {
    val balance = state.getPlayerBalance(player.username)
    context.parent ! PlayerRepository.Command.UpdateBalance(player.username, Balance(balance))
  }

  def broadcast(players: Map[ActorRef, Player], msg: OutgoingMessage): Unit = {
    players.foreach { case (ref, _) => ref ! msg}
  }

}
