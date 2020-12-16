package com.dmiva.sicbo.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Status}
import com.dmiva.sicbo.actors.repository.PlayerRepository
import com.dmiva.sicbo.actors.repository.PlayerRepository.LoginResult
import com.dmiva.sicbo.common.IncomingMessage.{Login, Logout, PlaceBet, Register}
import com.dmiva.sicbo.common.OutgoingMessage.{Error, LoggedOut, LoginFailed, LoginSuccessful}
import com.dmiva.sicbo.common.{ErrorMessage, IncomingMessage, OutgoingMessage}
import com.dmiva.sicbo.domain.Player
import com.dmiva.sicbo.domain.Player.Player

object User {
  case class Connected(wsHandle: ActorRef)
  case object Disconnected

  def props(lobby: ActorRef) = Props(new User(lobby))
}

class User(lobby: ActorRef) extends Actor with ActorLogging {
  import User._

  override def receive: Receive = waitingForConnection()

  private def waitingForConnection(): Receive = {

    case Connected(wsHandle) =>
      log.info(s"${wsHandle.toString()} connected")
      context.become(connected(wsHandle))
  }

  private def connected(wsHandle: ActorRef): Receive = {

    case Disconnected => {
      wsHandle ! Status.Success
      context.stop(self)
    }

    case msg: IncomingMessage => msg match {
      case Register(username, password)   => lobby ! PlayerRepository.Command.Register(username, password)
      case Login(username, password)      => lobby ! PlayerRepository.Command.Login(username, password)
      case Logout(_) => wsHandle ! Error(ErrorMessage.NotLoggedIn)
      case _ => wsHandle ! OutgoingMessage.Error("Invalid request")
    }

    case msg: PlayerRepository.LoginResult => msg match {
      case LoginResult.UserDoesNotExist => wsHandle ! LoginFailed // TODO: Send reason
      case LoginResult.PasswordIncorrect => wsHandle ! LoginFailed
      case LoginResult.Successful(user) =>
        lobby ! GameRoom.Join(user, self) // Automatic join to game because it's the only game room
        wsHandle ! LoginSuccessful
        context.become(loggedIn(wsHandle, user))
    }

    case msg: OutgoingMessage => wsHandle ! msg

    case _ => wsHandle ! OutgoingMessage.Error("Invalid request")

  }

  private def loggedIn(wsHandle: ActorRef, player: Player): Receive = { // TODO: Rename User
    case Disconnected => {
      wsHandle ! Status.Success
      context.stop(self)
    }

    case msg: IncomingMessage => msg match {
      case bet: PlaceBet => lobby ! bet
      case Login(_, _) => wsHandle ! Error(ErrorMessage.AlreadyLoggedIn)
      case Logout(_) =>
        lobby ! GameRoom.Leave(player, self)
        wsHandle ! LoggedOut
        context.become(connected(wsHandle))
      case _ => wsHandle ! OutgoingMessage.Error("Invalid request")
    }

    case msg: OutgoingMessage => wsHandle ! msg

    case _ => wsHandle ! OutgoingMessage.Error("Invalid request")

  }
}
