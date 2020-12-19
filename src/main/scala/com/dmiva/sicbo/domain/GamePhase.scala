package com.dmiva.sicbo.domain

import com.fasterxml.jackson.core.{JsonGenerator, JsonParser}
import com.fasterxml.jackson.databind.{DeserializationContext, SerializerProvider}
import com.fasterxml.jackson.databind.annotation.{JsonDeserialize, JsonSerialize}
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer

import java.time.Instant

@JsonSerialize(using = classOf[GamePhaseJsonSerializer])
@JsonDeserialize(using = classOf[GamePhaseJsonDeserializer])
sealed trait GamePhase
object GamePhase {
  case object Idle extends GamePhase
  case object PlacingBets extends GamePhase
  case object RollingDice extends GamePhase
  case object MakePayouts extends GamePhase
  case object GameEnded extends GamePhase
}

/** Used in Akka serialization (snapshots) */
class GamePhaseJsonSerializer extends StdSerializer[GamePhase](classOf[GamePhase]) {
  import GamePhase._

  override def serialize(value: GamePhase, gen: JsonGenerator, provider: SerializerProvider): Unit = {
    val strValue = value match {
      case Idle         => "idle"
      case PlacingBets  => "placing_bets"
      case RollingDice  => "rolling_dice"
      case MakePayouts  => "make_payouts"
      case GameEnded    => "game_ended"
    }
    gen.writeString(strValue)
  }
}

/** Used in Akka serialization (snapshots) */
class GamePhaseJsonDeserializer extends StdDeserializer[GamePhase](classOf[GamePhase]) {
  import GamePhase._

  override def deserialize(p: JsonParser, ctxt: DeserializationContext): GamePhase = {
    p.getText match {
      case "idle"         => Idle
      case "placing_bets" => PlacingBets
      case "rolling_dice" => RollingDice
      case "make_payouts" => MakePayouts
      case "game_ended"   => GameEnded
    }
  }
}