package com.dmiva.sicbo.actors

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props, Timers}
import com.dmiva.sicbo.actors.DiceRoller.DiceResult
import com.dmiva.sicbo.common.OutgoingMessage.{BetAccepted, BetRejected, Error, GamePhaseChanged}
import com.dmiva.sicbo.common.{BetRejectReason, ErrorMessage, OutgoingMessage}
import com.dmiva.sicbo.domain.{Bet, GamePhase, GameState}
import com.dmiva.sicbo.domain.Player.{Player, PlayerSession}

import scala.collection.IterableOnce.iterableOnceExtensionMethods
import scala.concurrent.duration.DurationInt

object GameRoom {

//  case class Join(player: Player, ref: ActorRef)
  case class Join(ref: ActorRef, session: PlayerSession)
  case class Leave(ref: ActorRef)
  case class PlaceBet(player: Player, bets: List[Bet])

  def props() = Props(new GameRoom)
}

class GameRoom extends Actor with Timers with ActorLogging {
  import GameRoom._

  val diceRoller: ActorRef = context.actorOf(DiceRoller.props())

  var state: GameState = GameState.initGame

  // Supervision strategy for the dice roller actor
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 2, withinTimeRange = 4.seconds) {
      case _: IllegalStateException    => Restart
      case _: Exception                => Escalate
    }

  override def receive: Receive = playing(Map())

//  private def playing(users: Set[ActorRef]): Receive = {
  private def playing(users: Map[ActorRef, PlayerSession]): Receive = {

    case Join(ref, session) =>
      log.info(s"${session.player.username} joined the game.")
      if (state.phase == GamePhase.Idle) timers.startSingleTimer("Starting game...", GamePhase.PlacingBets, 3.seconds)
      context.become(playing(users + (ref -> session)))

    case Leave(ref) =>
      users.get(ref) match {
        case None => ref ! Error(ErrorMessage.NotLoggedIn)
        case Some(session) =>
          log.info(s"${session.player.username} left the game.")

          context.become(playing(users - ref))
      }

    case PlaceBet(player, bets) =>
      if (state.phase == GamePhase.PlacingBets && users.contains(sender())) {
        state.placeBet(player, bets) match {
          case Right(newState) =>
            state = newState
            sender() ! BetAccepted
            bets.map(bet => println(s"${player.username} placed bet with type = ${bet.betType} and amount = ${bet.amount}"))
          case Left(e) => sender() ! BetRejected(BetRejectReason.NotEnoughBalance)
        }
        //        sender() ! BetAccepted
      }
      else
        if (users.contains(sender())) sender() ! BetRejected(BetRejectReason.TimeExpired)
        else sender() ! Error(ErrorMessage.NotLoggedIn)


    case DiceRoller.DiceResult(dice) =>
      log.info(s"Received dice result $dice")
      state.applyDiceOutcome(dice) match {
        case Right(newState) => state = newState
        case Left(e) => log.error("Fatal error")
      }

    case GamePhase.Idle =>
      state = GameState.initGame
      println("Waiting for players...")

    case GamePhase.PlacingBets =>
      state = state.startNewGame
      broadcast(users,GamePhaseChanged(state.phase))

      println("New game started. You have 15 seconds to place bets...")
      timers.startSingleTimer("Time before rolling dice", GamePhase.RollingDice, 15.seconds)

    case GamePhase.RollingDice =>
      state = state.setGamePhase(GamePhase.RollingDice)
      broadcast(users,GamePhaseChanged(state.phase))

      println("Dice are rolling...")
      diceRoller ! DiceRoller.RollDice
      timers.startSingleTimer("Time before making payouts", GamePhase.MakePayouts, 5.seconds)

    case GamePhase.MakePayouts =>
      state = state.setGamePhase(GamePhase.MakePayouts)
      broadcast(users,GamePhaseChanged(state.phase))
      println("Making payouts...")
      state.calculateResults match {
        case Right(newState) => state = newState
        case Left(e) => log.error("Something bad happened")
      }
      println(state.gameResult)

      state.gameResult.foreachEntry {
        case (player, result) =>
          users.find { case (_, session) => session.player == player } match {
            case Some((ref, session)) =>
              ref ! result
            case None =>
          }
      }


      timers.startSingleTimer("Time before new round", GamePhase.GameEnded, 5.seconds)

    case GamePhase.GameEnded =>
      println("Game ended.")
      if (users.nonEmpty)
        self ! GamePhase.PlacingBets
      else
        self ! GamePhase.Idle
  }

  def broadcast(players: Map[ActorRef, PlayerSession], msg: OutgoingMessage): Unit = {
    players.foreach(player => player._1 ! msg) // TODO: Refactor
  }

}
