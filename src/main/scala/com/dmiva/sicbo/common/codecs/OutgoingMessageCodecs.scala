package com.dmiva.sicbo.common.codecs

import akka.http.scaladsl.model.ws.TextMessage
import com.dmiva.sicbo.common.OutgoingMessage
import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.semiauto._
import com.dmiva.sicbo.common.JsonConfig.customConfig
import com.dmiva.sicbo.common.OutgoingMessage.GameResult
import com.dmiva.sicbo.domain.DiceOutcome
import com.dmiva.sicbo.domain.Player.{Balance, PlayerInfo, UserType}
import io.circe.syntax.EncoderOps

object OutgoingMessageCodecs {
  implicit val outgoingMessageEncoder: Encoder[OutgoingMessage] = deriveConfiguredEncoder
  implicit val outgoingMessageDecoder: Decoder[OutgoingMessage] = deriveConfiguredDecoder

  implicit val gameResultEncoder: Encoder[GameResult] = deriveConfiguredEncoder
  implicit val gameResultDecoder: Decoder[GameResult] = deriveConfiguredDecoder

  implicit val playerInfoEncoder: Encoder[PlayerInfo] = deriveConfiguredEncoder
  implicit val playerInfoDecoder: Decoder[PlayerInfo] = deriveConfiguredDecoder

  implicit val diceOutcomeEncoder: Encoder[DiceOutcome] = deriveConfiguredEncoder
  implicit val diceOutcomeDecoder: Decoder[DiceOutcome] = deriveConfiguredDecoder

  implicit val userTypeEncoder: Encoder[UserType] = deriveConfiguredEncoder
  implicit val userTypeDecoder: Decoder[UserType] = deriveConfiguredDecoder

  implicit val balanceEncoder: Encoder[Balance] = deriveConfiguredEncoder
  implicit val balanceDecoder: Decoder[Balance] = deriveConfiguredDecoder

  implicit class outgoingMessageOps(message: OutgoingMessage) {
    def toText: String = message.asJson.toString()
    def toTextMessage: TextMessage = TextMessage.Strict(message.asJson.toString())
  }
}
