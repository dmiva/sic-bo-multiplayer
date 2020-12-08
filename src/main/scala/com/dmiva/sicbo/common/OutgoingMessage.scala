package com.dmiva.sicbo.common

import io.circe.generic.extras.ConfiguredJsonCodec
import JsonConfig.customConfig

// Messages from server to client
@ConfiguredJsonCodec sealed trait OutgoingMessage

case object LoginSuccessful extends OutgoingMessage
case object LoginFailed extends OutgoingMessage

case class Error(text: String) extends OutgoingMessage

