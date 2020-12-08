package com.dmiva.sicbo.common.codecs

import akka.http.scaladsl.model.ws.TextMessage
import com.dmiva.sicbo.common.OutgoingMessage
import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.semiauto._
import com.dmiva.sicbo.common.JsonConfig.customConfig
import io.circe.syntax.EncoderOps

object OutgoingMessageCodecs {
  implicit val outgoingMessageEncoder: Encoder.AsObject[OutgoingMessage] = deriveConfiguredEncoder
  implicit val outgoingMessageDecoder: Decoder[OutgoingMessage] = deriveConfiguredDecoder

  implicit class outgoingMessageOps(message: OutgoingMessage) {
    def toText: String = message.asJson.toString()
    def toTextMessage: TextMessage = TextMessage.Strict(message.asJson.toString())
  }
}
