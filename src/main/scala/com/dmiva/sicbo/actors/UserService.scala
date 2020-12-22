package com.dmiva.sicbo.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.pipe
import com.dmiva.sicbo.actors.UserService.Command.{Login, LoginRequestResult, UpdateBalance, UpdateBalanceResult}
import com.dmiva.sicbo.actors.UserService.LoginResult
import com.dmiva.sicbo.common.IncomingMessage
import com.dmiva.sicbo.domain.{Balance, Name, Password, Player}
import com.dmiva.sicbo.service.PlayerService

import scala.concurrent.ExecutionContext.Implicits.global

object UserService {

  sealed trait Command
  object Command {
    final case class Login(username: Name, password: Password) extends Command
    final case class LoginRequestResult(player: Option[Player], password: Password, replyTo: ActorRef) extends Command
    final case class UpdateBalance(username: Name, balance: Balance) extends Command
    final case class UpdateBalanceResult(int: Int, replyTo: ActorRef) extends Command
  }

  sealed trait LoginResult
  object LoginResult {
    final case class Successful(player: Player) extends LoginResult
    case object UserDoesNotExist extends LoginResult
    case object PasswordIncorrect extends LoginResult

    implicit def convertToString(msg: LoginResult): String = msg.toString
  }

  def props(service: PlayerService): Props = Props(new UserService(service))

}

class UserService(service: PlayerService) extends Actor with ActorLogging {

  override def receive: Receive = {
    // Login through database
    case Login(name, pw) =>
      val replyTo = sender()
      val futureResult = service.loginPlayer(IncomingMessage.Login(name, pw))
        .map(result => LoginRequestResult(result, pw, replyTo))
      futureResult pipeTo self

    case LoginRequestResult(player, pw, replyTo) =>
      val loginResult = player match {
        case Some(player) if player.password == pw  => LoginResult.Successful(player)
        case Some(_)                                => LoginResult.PasswordIncorrect
        case None                                   => LoginResult.UserDoesNotExist
      }
      replyTo ! loginResult

    // Update balance through database
    case UpdateBalance(name, balance) =>
      val replyTo = sender()
      val futureResult = service.updateBalance(name, balance)
        .map(result => UpdateBalanceResult(result, replyTo))
      futureResult pipeTo self

    case UpdateBalanceResult(_, _) => // do nothing

  }

}