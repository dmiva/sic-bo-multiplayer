package com.dmiva.sicbo.actors

import akka.Done
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.dmiva.sicbo.actors.repository.PlayerRepository
import com.dmiva.sicbo.actors.repository.PlayerRepository.LoginResult
import com.dmiva.sicbo.common.IncomingMessage.{Login, Logout, PlaceBet, Register}
import com.dmiva.sicbo.common.OutgoingMessage.{Error, LoggedOut, LoginFailed, LoginSuccessful}
import com.dmiva.sicbo.common.{IncomingMessage, OutgoingMessage}
import com.dmiva.sicbo.domain.{Player, PlayerInfo}

object User {

  sealed trait ErrorMessage
  object ErrorMessage {
    val NotLoggedIn = "User is not logged in"
    val AlreadyLoggedIn = "Login attempt when already logged in"
    val InvalidRequest = "Invalid request"
  }

  final case class Connected(wsHandle: ActorRef)
  case object Disconnected

  def props(lobby: ActorRef) = Props(new User(lobby))
}

class User(lobby: ActorRef) extends Actor with ActorLogging {
  import User._

  override def receive: Receive = waitingForConnection()

  private def waitingForConnection(): Receive = {

    case Connected(wsHandle) => log.info(s"${wsHandle.toString()} connected")
                                context.become(connected(wsHandle))
  }

  /**
   * At this state WebSocket connection with server is just established.
   * @param wsHandle All messages sent to this actor goes to websocket channel to the client
   */
  private def connected(wsHandle: ActorRef): Receive = {

    case Disconnected =>
      wsHandle ! Done
      context.stop(self)

    case msg: IncomingMessage => msg match {
//      case Register(username, password)    => lobby ! PlayerRepository.Command.Register(username, password)
      case Login(username, password)       => lobby ! PlayerRepository.Command.Login2(username, password)
      case _                               => wsHandle ! Error(ErrorMessage.InvalidRequest)
    }

    case msg: PlayerRepository.LoginResult => msg match {
      case LoginResult.UserDoesNotExist    => wsHandle ! LoginFailed // TODO: Send reason
      case LoginResult.PasswordIncorrect   => wsHandle ! LoginFailed
      case LoginResult.Successful(user)    => // Automatic join to game because it's the only game room
                                              lobby ! GameRoom.Command.Join(self, user)
                                              val playerInfo = PlayerInfo.from(user)
                                              wsHandle ! LoginSuccessful(playerInfo)
                                              context.become(loggedIn(wsHandle, user))
    }

    case msg: OutgoingMessage              => wsHandle ! msg
    case _                                 => wsHandle ! Error(ErrorMessage.InvalidRequest)

  }

  /** At this state user is in the game room */
  private def loggedIn(wsHandle: ActorRef, player: Player): Receive = {
    // When stream
    case Disconnected         => wsHandle ! Done
                                 lobby ! GameRoom.Command.Leave(self, player)
                                 context.stop(self)
    // Messages
    case msg: IncomingMessage => msg match {
      case msg: PlaceBet      => lobby ! GameRoom.Command.PlaceBet(self, player, msg.bets)
      case Logout(_)          => lobby ! GameRoom.Command.Leave(self, player)
                                 wsHandle ! LoggedOut
                                 context.become(connected(wsHandle))
      case Login(_,_)         => wsHandle ! Error(ErrorMessage.AlreadyLoggedIn)
      case _                  => wsHandle ! Error(ErrorMessage.InvalidRequest)
    }

    case msg: OutgoingMessage => wsHandle ! msg
    case _                    => wsHandle ! Error(ErrorMessage.InvalidRequest)
  }

}
