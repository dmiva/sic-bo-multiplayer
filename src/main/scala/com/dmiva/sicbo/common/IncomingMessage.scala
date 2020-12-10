package com.dmiva.sicbo.common

import com.dmiva.sicbo.domain.Bet

// Messages from client to server
sealed trait IncomingMessage
object IncomingMessage {
  final case class Login(username: String) extends IncomingMessage
  final case class Logout(username: String) extends IncomingMessage
  final case class PlaceBet(bets: List[Bet]) extends IncomingMessage
}
