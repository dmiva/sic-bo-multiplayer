package com.dmiva.sicbo.domain

import com.dmiva.sicbo.common.JsonConfig.customConfig
import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}

sealed trait BetType

object BetType {
  case object Small extends BetType
  case object Big extends BetType
  case object Even extends BetType
  case object Odd extends BetType
  case class Number(num: Int) extends BetType
  case class Total(num: Int) extends BetType
  case class Combo(a: Int, b: Int) extends BetType
  case class Double(num: Int) extends BetType
  case class Triple(num: Int) extends BetType
  case object AnyTriple extends BetType

  implicit val betTypeEncoder: Encoder.AsObject[BetType] = deriveConfiguredEncoder[BetType]
  implicit val betTypeDecoder: Decoder[BetType] = deriveConfiguredDecoder[BetType]
}
