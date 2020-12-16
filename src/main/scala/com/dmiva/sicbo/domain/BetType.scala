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
  case class Combo(a: Int, b: Int) extends BetType /*{
    override def equals(obj: Any): Boolean = {
      obj match {
        case other: Combo => (a == other.a && b == other.b) || (a == other.b && b == other.a)
        case _ => super.equals(obj)
      }
    }
  }*/
  case class Double(num: Int) extends BetType
  case class Triple(num: Int) extends BetType
  case object AnyTriple extends BetType

  implicit val betTypeEncoder: Encoder.AsObject[BetType] = deriveConfiguredEncoder[BetType]
  implicit val betTypeDecoder: Decoder[BetType] = deriveConfiguredDecoder[BetType]

  private val numberBets: List[BetType] =
    Number(1) :: Number(2) :: Number(3) :: Number(4) :: Number(5) :: Number(6) :: Nil

  private val totalBets: List[BetType] =
    Total(4) :: Total(5) :: Total(6) ::
    Total(7) :: Total(8) :: Total(9) ::
    Total(10) :: Total(11) :: Total(12) ::
    Total(13) :: Total(14) :: Total(15) ::
    Total(16) :: Total(17) :: Nil

  private val comboBets: List[BetType] =
    Combo(1,2) :: Combo(1,3) :: Combo(1,4) :: Combo(1,5) :: Combo(1,6) ::
    Combo(2,3) :: Combo(2,4) :: Combo(2,5) :: Combo(2,6) ::
    Combo(3,4) :: Combo(3,5) :: Combo(3,6) ::
    Combo(4,5) :: Combo(4,6) ::
    Combo(5,6) :: Nil

  private val doubleBets: List[BetType] =
    Double(1) :: Double(2) :: Double(3) :: Double(4) :: Double(5) :: Double(6) :: Nil

  private val tripleBets: List[BetType] =
    Triple(1) :: Triple(2) :: Triple(3) :: Triple(4) :: Triple(5) :: Triple(6) :: Nil

  val allBets: List[BetType] =
    Small :: Big :: Even :: Odd :: AnyTriple ::
    numberBets ::: totalBets ::: comboBets ::: doubleBets ::: tripleBets ::: Nil

}
