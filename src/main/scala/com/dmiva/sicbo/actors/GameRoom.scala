package com.dmiva.sicbo.actors

import akka.actor.{Actor, ActorRef, Props, Terminated}
import com.dmiva.sicbo.common.{Error, LoginSuccessful}

object GameRoom {

  case class Join(username: String, user: ActorRef)

  def props() = Props(new GameRoom)
}

class GameRoom extends Actor {
  import GameRoom._


  override def receive: Receive = run(Set.empty)

  private def run(users: Set[ActorRef]): Receive = {

    case Join(username, user) =>
      if (users.contains(user)) {
        // do nothing at this moment
        user ! Error("Login attempt when already logged in")
      } else {
        user ! LoginSuccessful
        context.watch(user)
        println(s"$username joined the game")
        context.become(run(users + user))
      }

    case Terminated(ref: ActorRef) =>
      run(users.filterNot(_ == ref))

  }


}


