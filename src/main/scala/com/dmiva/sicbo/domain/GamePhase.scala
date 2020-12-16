package com.dmiva.sicbo.domain

sealed trait GamePhase
object GamePhase {
  case object Idle extends GamePhase
  case object PlacingBets extends GamePhase
  case object RollingDice extends GamePhase
  case object MakePayouts extends GamePhase
  case object GameEnded extends GamePhase
}
