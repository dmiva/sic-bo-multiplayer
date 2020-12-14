package com.dmiva.sicbo.common

import com.dmiva.sicbo.domain.Bet
import com.dmiva.sicbo.domain.Player.{Name, Password}

// Messages from client to server
sealed trait IncomingMessage
object IncomingMessage {
  final case class Register(username: Name, password: Password) extends IncomingMessage
  final case class Login(username: Name, password: Password) extends IncomingMessage
  final case class Logout(username: Name) extends IncomingMessage
  final case class PlaceBet(bets: List[Bet]) extends IncomingMessage
}
