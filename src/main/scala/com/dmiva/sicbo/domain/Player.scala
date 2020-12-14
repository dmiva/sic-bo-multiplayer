package com.dmiva.sicbo.domain

import akka.actor.ActorRef

object Player {

//  sealed trait Currency
//  object Currency {
//    case object EUR extends Currency
//    case object USD extends Currency
//    case object RUB extends Currency
//  }
//  case class Balance(amount: Int, currency: Currency)

  sealed trait UserType
  object UserType {
    case object User extends UserType
    case object Admin extends UserType
  }
  type Name = String
  type Password = String

  case class Balance(amount: BigDecimal) extends AnyVal

  case class User(id: Long, username: Name, password: Password, userType: UserType)

  case class Player(id: Long, username: Name, balance: Balance)

  case class PlayerSession(player: Player, ref: ActorRef)
}
