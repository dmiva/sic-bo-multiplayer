package com.dmiva.sicbo.domain

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
