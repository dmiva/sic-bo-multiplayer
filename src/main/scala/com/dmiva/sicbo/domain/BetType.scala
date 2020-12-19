package com.dmiva.sicbo.domain

import com.dmiva.sicbo.common.JsonConfig.customConfig
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo, JsonTypeName}
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}

// For Akka persistence serialization (snapshots)
// Required because this trait has case classes and case objects
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[BetType.Small], name = "small"),
    new JsonSubTypes.Type(value = classOf[BetType.Big], name = "big"),
    new JsonSubTypes.Type(value = classOf[BetType.Even], name = "even"),
    new JsonSubTypes.Type(value = classOf[BetType.Odd], name = "odd"),
    new JsonSubTypes.Type(value = classOf[BetType.Number], name = "number"),
    new JsonSubTypes.Type(value = classOf[BetType.Total], name = "total"),
    new JsonSubTypes.Type(value = classOf[BetType.Combo], name = "combo"),
    new JsonSubTypes.Type(value = classOf[BetType.Double], name = "double"),
    new JsonSubTypes.Type(value = classOf[BetType.Triple], name = "triple"),
    new JsonSubTypes.Type(value = classOf[BetType.AnyTriple], name = "any_triple")))
sealed trait BetType
object BetType {

  @JsonTypeName("small")      case object Small extends Small
  @JsonTypeName("big")        case object Big extends Big
  @JsonTypeName("even")       case object Even extends Even
  @JsonTypeName("odd")        case object Odd extends Odd
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
  @JsonTypeName("any_triple") case object AnyTriple extends AnyTriple

  // Intermediate trait and a custom deserializer are needed for Akka persistence serialization
  // See https://doc.akka.io/docs/akka/current/serialization-jackson.html#adt-with-trait-and-case-object
  @JsonDeserialize(using = classOf[SmallDeserializer])      sealed trait Small extends BetType
  @JsonDeserialize(using = classOf[BigDeserializer])        sealed trait Big extends BetType
  @JsonDeserialize(using = classOf[EvenDeserializer])       sealed trait Even extends BetType
  @JsonDeserialize(using = classOf[OddDeserializer])        sealed trait Odd extends BetType
  @JsonDeserialize(using = classOf[AnyTripleDeserializer])  sealed trait AnyTriple extends BetType

  class SmallDeserializer extends StdDeserializer[Small](Small.getClass) {
    override def deserialize(p: JsonParser, ctxt: DeserializationContext): Small = Small
  }
  class BigDeserializer extends StdDeserializer[Big](Big.getClass) {
    override def deserialize(p: JsonParser, ctxt: DeserializationContext): Big = Big
  }
  class EvenDeserializer extends StdDeserializer[Even](Even.getClass) {
    override def deserialize(p: JsonParser, ctxt: DeserializationContext): Even = Even
  }
  class OddDeserializer extends StdDeserializer[Odd](Odd.getClass) {
    override def deserialize(p: JsonParser, ctxt: DeserializationContext): Odd = Odd
  }
  class AnyTripleDeserializer extends StdDeserializer[AnyTriple](AnyTriple.getClass) {
    override def deserialize(p: JsonParser, ctxt: DeserializationContext): AnyTriple = AnyTriple
  }

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
