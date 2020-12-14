package com.dmiva.sicbo.common

// Messages from server to client
sealed trait OutgoingMessage

object OutgoingMessage {
  case object LoginSuccessful extends OutgoingMessage
  case object LoginFailed extends OutgoingMessage
  case object LoggedOut extends OutgoingMessage
  case object BetAccepted extends OutgoingMessage
  case class BetRejected(reason: String) extends OutgoingMessage

  case class Error(text: String) extends OutgoingMessage
  case class Ok(text: String) extends OutgoingMessage

//  case object GameStateChanged extends OutgoingMessage
}

sealed trait BetRejectReason
object BetRejectReason {
  val NotEnoughBalance = "Not enough balance"
  val TimeExpired = "Wait for the next round"
}

sealed trait ErrorMessage
object ErrorMessage {
  val NotLoggedIn = "User is not logged in"
  val AlreadyLoggedIn = "Login attempt when already logged in"
}

