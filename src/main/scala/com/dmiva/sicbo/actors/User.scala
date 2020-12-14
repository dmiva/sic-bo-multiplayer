package com.dmiva.sicbo.actors

import akka.actor.{Actor, ActorRef, Props, Status}
import com.dmiva.sicbo.actors.repository.UserRepository
import com.dmiva.sicbo.common.IncomingMessage.{Login, Logout, PlaceBet, Register}
import com.dmiva.sicbo.common.{IncomingMessage, OutgoingMessage}

object User {
  case class Connected(wsHandle: ActorRef)
  case object Disconnected

  def props(lobby: ActorRef) = Props(new User(lobby))
}

class User(lobby: ActorRef) extends Actor {
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
      wsHandle ! Status.Success
      context.stop(self)
    }

    case msg: IncomingMessage => msg match {
      case Register(username, password)       => lobby ! UserRepository.Command.Register(username, password)
      case Login(username) => lobby ! GameRoom.Join(username, self)
      case Logout(username) => lobby ! GameRoom.Leave(username, self)
      case bet: PlaceBet => lobby ! bet
    }

    case msg: OutgoingMessage => wsHandle ! msg

  }
}
