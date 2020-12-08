package com.dmiva.sicbo.common

import JsonConfig.customConfig
import io.circe.generic.extras.ConfiguredJsonCodec

// Messages from server to client
@ConfiguredJsonCodec sealed trait OutgoingMessage

case object LoginSuccessful extends OutgoingMessage
case object LoginFailed extends OutgoingMessage
case object LoggedOut extends OutgoingMessage

case object BetAccepted extends OutgoingMessage

case class BetRejected(reason: String) extends OutgoingMessage

sealed trait BetRejectReason
object BetRejectReason {
  val notEnoughBalance = "Not enough balance"
  val timeExpired = "Wait for the next round"
}

case class Error(text: String) extends OutgoingMessage
sealed trait ErrorMessage
object ErrorMessage {
  val notLoggedIn = "User is not logged in"
  val alreadyLoggedIn = "Login attempt when already logged in"
}

