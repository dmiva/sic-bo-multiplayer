package com.dmiva.sicbo.common.codecs

import akka.http.scaladsl.model.ws.TextMessage
import com.dmiva.sicbo.common.IncomingMessage
import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.semiauto._
import com.dmiva.sicbo.common.JsonConfig.customConfig
import io.circe.syntax.EncoderOps

object IncomingMessageCodecs {
  implicit val incomingMessageEncoder: Encoder.AsObject[IncomingMessage] = deriveConfiguredEncoder
  implicit val incomingMessageDecoder: Decoder[IncomingMessage] = deriveConfiguredDecoder

  implicit class incomingMessageOps(message: IncomingMessage) {
    def toText: String = message.asJson.toString()
  }

}
