package com.dmiva.sicbo.common.codecs

import com.dmiva.sicbo.common.IncomingMessage
import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.semiauto._
import com.dmiva.sicbo.common.JsonConfig.customConfig
import com.dmiva.sicbo.domain.Bet
import io.circe.syntax.EncoderOps

object IncomingMessageCodecs {
//  implicit val incomingMessageEncoder: Encoder.AsObject[IncomingMessage] = deriveConfiguredEncoder
  implicit val incomingMessageEncoder: Encoder[IncomingMessage] = deriveConfiguredEncoder
  implicit val incomingMessageDecoder: Decoder[IncomingMessage] = deriveConfiguredDecoder

  implicit val betEncoder: Encoder.AsObject[Bet] = deriveConfiguredEncoder[Bet]
  implicit val betDecoder: Decoder[Bet] = deriveConfiguredDecoder[Bet]

  implicit class incomingMessageOps(message: IncomingMessage) {
    def toText: String = message.asJson.toString()
  }

}
