package com.dmiva.sicbo.actors

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props, Terminated, Timers}
import com.dmiva.sicbo.actors.DiceRoller.DiceResult
import com.dmiva.sicbo.common.IncomingMessage.PlaceBet
import com.dmiva.sicbo.common.OutgoingMessage.{BetAccepted, BetRejected, Error}
import com.dmiva.sicbo.common.{BetRejectReason, ErrorMessage}
import com.dmiva.sicbo.domain.GameState
import com.dmiva.sicbo.domain.Player.Player

import scala.concurrent.duration.DurationInt

object GameRoom {

  case class Join(player: Player, ref: ActorRef)
  case class Leave(player: Player, ref: ActorRef)

  def props() = Props(new GameRoom)
}

class GameRoom extends Actor with Timers with ActorLogging {
  import GameRoom._

  val direRoller: ActorRef = context.actorOf(DiceRoller.props())

  var gameStateOld: Game = Game(IdleOld)
  var state: GameState = GameState.initGame

  // Supervision strategy for the dice roller actor
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 2, withinTimeRange = 4.seconds) {
      case _: IllegalStateException    => Restart
      case _: Exception                => Escalate
    }

  override def receive: Receive = idle(Set.empty)

  // Initial state of room
//  private def waitingForPlayers(players: Map[ActorRef, PlayerSession]): Receive = {
//
//    case Join(player, ref) =>
//
//
//
//  }

  private def idle(users: Set[ActorRef]): Receive = {

    case Join(player, ref) =>
      if (users.contains(ref)) {
        // do nothing at this moment
        ref ! Error(ErrorMessage.AlreadyLoggedIn)
      } else {
//        ref ! LoginSuccessful
        context.watch(ref)
        println(s"${player.username} joined the game. Players: ${users.size+1}")
        if (gameStateOld == Game(IdleOld)) timers.startSingleTimer("Starting game...", PlacingBetsOld, 3.seconds)
        context.become(idle(users + ref))
      }

    case Leave(player, ref) =>
      if (users.contains(ref)) {
//        ref ! LoggedOut
        println(s"${player.username} left the game. Players: ${users.size-1}") // TODO: Should search by user reference, not by name
        context.become(idle(users.filterNot(_ == ref)))
      } else {
        ref ! Error(ErrorMessage.NotLoggedIn)
      }

    case PlaceBet(bets) =>
      if (gameStateOld == Game(PlacingBetsOld) && users.contains(sender())) {
        sender() ! BetAccepted
        bets.map(bet => println(s"Someone placed bet with type = ${bet.betType} and amount = ${bet.amount}"))
      }
      else if (users.contains(sender())) sender() ! BetRejected(BetRejectReason.TimeExpired)
        else sender() ! Error(ErrorMessage.NotLoggedIn)


    case Terminated(ref: ActorRef) => // TODO: Should be handled by someone else
      println(s"Someone disconnected the game. Players: ${users.size-1}")
      context.become(idle(users.filterNot(_ == ref)))


    case DiceRoller.DiceResult(dice) => log.info(s"Received dice result $dice")



    case IdleOld =>
      gameStateOld = Game(IdleOld)
      println("Waiting for players...")

    case PlacingBetsOld =>
      gameStateOld = Game(PlacingBetsOld)
      println("New game started. You have 15 seconds to place bets...")
      timers.startSingleTimer("Time before rolling dice", RollingDiceOld, 15.seconds)

    case RollingDiceOld =>
      gameStateOld = Game(RollingDiceOld)
      println("Dice are rolling...")
      direRoller ! DiceRoller.RollDice // TODO: Handle receive event DiceResult
      timers.startSingleTimer("Time before making payouts", MakePayoutsOld, 5.seconds)

    case MakePayoutsOld =>
      gameStateOld = Game(MakePayoutsOld)
      println("Making payouts...")
      timers.startSingleTimer("Time before new round", GameEndedOld, 5.seconds)

    case GameEndedOld =>
      println("Game ended.")
      if (users.nonEmpty)
        self ! PlacingBetsOld
      else
        self ! IdleOld
  }

//  private def playing(users: Set[ActorRef]): Receive = {
//
//  }


}

object Game {

}

case class Game(currentState: GameStateOld)

sealed trait GameStateOld

case object IdleOld extends GameStateOld
case object PlacingBetsOld extends GameStateOld
case object RollingDiceOld extends GameStateOld
case object MakePayoutsOld extends GameStateOld
case object GameEndedOld extends GameStateOld

