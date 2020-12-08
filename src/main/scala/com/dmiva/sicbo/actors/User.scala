package com.dmiva.sicbo.actors

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import com.dmiva.sicbo.common.{IncomingMessage, Login, Logout, OutgoingMessage, PlaceBet}

object User {
  case class Connected(wsHandle: ActorRef)
  case object Disconnected

  def props(gameRoom: ActorRef) = Props(new User(gameRoom))
}

class User(gameRoom: ActorRef) extends Actor {
  import User._


  override def receive: Receive = waitingForConnection()

  private def waitingForConnection(): Receive = {

    case Connected(wsHandle) =>
      // Passthrough security checks TODO: check if user exists
      //gameRoom ! GameRoom.Join(self)
      println(s"$wsHandle connected")
      context.become(connected(wsHandle))

  }

  private def connected(wsHandle: ActorRef): Receive = {

    case Disconnected => {
      wsHandle ! PoisonPill
      context.stop(self)
    }

    case msg: IncomingMessage => msg match {
      case Login(username) => gameRoom ! GameRoom.Join(username, self)
      case Logout(username) => gameRoom ! GameRoom.Leave(username, self)
      case bet: PlaceBet => gameRoom ! bet
    }

    case msg: OutgoingMessage => wsHandle ! msg

  }
}
