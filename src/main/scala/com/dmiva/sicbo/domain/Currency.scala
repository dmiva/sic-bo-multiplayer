package com.dmiva.sicbo.domain

/** Not used */
sealed trait Currency
object Currency {
  case object EUR extends Currency
  case object USD extends Currency
  case object RUB extends Currency
  case object GBP extends Currency
}
