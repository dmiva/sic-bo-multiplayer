package com.dmiva.sicbo.domain

object Player {

//  sealed trait Currency
//  object Currency {
//    case object EUR extends Currency
//    case object USD extends Currency
//    case object RUB extends Currency
//  }

//  case class Balance(amount: Int, currency: Currency)

  class Player(id: String, name: String, balance: Int)

}
