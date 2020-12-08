package com.dmiva.sicbo.actors

import akka.actor.{Actor, ActorRef, Props, Terminated, Timers}
import com.dmiva.sicbo.common.{BetRejected, Error, LoggedOut, LoginFailed, LoginSuccessful, NotEnoughBalance}

import scala.concurrent.duration.DurationInt

object GameRoom {

  case class Join(username: String, user: ActorRef)
  case class Leave(username: String, user: ActorRef)


  def props() = Props(new GameRoom)
}

class GameRoom extends Actor with Timers {
  import GameRoom._


  override def receive: Receive = idle(Set.empty)

  private def idle(users: Set[ActorRef]): Receive = {

    case Join(username, user) =>
      if (users.contains(user)) {
        // do nothing at this moment
        user ! Error("Login attempt when already logged in")
      } else {
        user ! LoginSuccessful
        context.watch(user)
        println(s"$username joined the game. Players: ${users.size+1}")
        if (users.isEmpty) timers.startSingleTimer("Starting game...", PlacingBets, 3.seconds)
        context.become(idle(users + user))
      }

    case Leave(username, user) =>
      if (users.contains(user)) {
        user ! LoggedOut
        println(s"$username left the game. Players: ${users.size-1}")
        context.become(idle(users.filterNot(_ == user)))
      } else {
        user ! Error("User is not logged in")
      }

    case Terminated(ref: ActorRef) =>
      context.become(idle(users.filterNot(_ == ref)))


    case PlacingBets =>
      println("Place bets started. You have 15 seconds to place bets")
      timers.startSingleTimer("Time before rolling dice", RollingDice, 15.seconds)

    case RollingDice =>
      println("Dice are rolling...")
      timers.startSingleTimer("Time before making payouts", MakePayouts, 5.seconds)

    case MakePayouts =>
      println("Making payouts...")
      timers.startSingleTimer("Time before new round", GameIsEnded, 5.seconds)

    case GameIsEnded =>
      println("Round ended.")
      if (users.nonEmpty) self ! PlacingBets

  }


}

object Game {

}

class Game(newState: GameState) {

}

sealed trait GameState

case object PlacingBets extends GameState
case object RollingDice extends GameState
case object MakePayouts extends GameState
case object GameIsEnded extends GameState

