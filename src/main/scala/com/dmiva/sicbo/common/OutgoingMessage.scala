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

case object NotEnoughBalance
case object TimeExpired

case class Error(text: String) extends OutgoingMessage

