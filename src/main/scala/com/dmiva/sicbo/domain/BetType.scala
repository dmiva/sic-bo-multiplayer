package com.dmiva.sicbo.domain

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
}
