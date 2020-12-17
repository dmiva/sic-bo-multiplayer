package com.dmiva.sicbo.common

import com.dmiva.sicbo.domain.{Bet, Name, Password}
import io.circe.Printer

// Messages from client to server
sealed trait IncomingMessage
object IncomingMessage {
  final case class Register(username: Name, password: Password) extends IncomingMessage
  final case class Login(username: Name, password: Password) extends IncomingMessage
  final case class Logout(username: Name) extends IncomingMessage
  final case class PlaceBet(bets: List[Bet]) extends IncomingMessage

  implicit val customPrinter = Printer.spaces2.copy(dropNullValues = true)
}
