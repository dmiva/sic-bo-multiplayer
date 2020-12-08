package com.dmiva.sicbo.common

// Messages from client to server
sealed trait IncomingMessage
object IncomingMessage {
  final case class Login(username: String) extends IncomingMessage
  final case class Logout(username: String) extends IncomingMessage
  final case class PlaceBet(betId: Int, amount: Int) extends IncomingMessage
}
