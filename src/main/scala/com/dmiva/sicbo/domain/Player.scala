package com.dmiva.sicbo.domain

import akka.actor.ActorRef

object Player { // TODO: Tidy up this mess

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
//  type Error = String

  case class Balance(amount: BigDecimal) extends AnyVal {
    def +(other: Balance): Balance = {
      copy(amount = amount + other.amount)
    }

//    implicit def intToBigDecimal(int: Int): BigDecimal = BigDecimal(int)
  }

  case class Player(id: Long, username: Name, password: Password, userType: UserType, balance: Balance)

  case class PlayerInfo(id: Long, username: Name, userType: UserType, balance: Balance)
  object PlayerInfo {
    def from(player: Player): PlayerInfo = {
      PlayerInfo(
        id = player.id,
        username = player.username,
        userType = player.userType,
        balance = player.balance
      )
    }
  }

  case class PlayerSession(player: Player, ref: ActorRef)
}
