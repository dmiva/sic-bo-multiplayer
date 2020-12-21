package com.dmiva.sicbo.service

import com.dmiva.sicbo.common.IncomingMessage.Register
import com.dmiva.sicbo.domain.{Balance, Player, UserType}
import com.dmiva.sicbo.repository.PlayerRepository

import scala.concurrent.Future

class PlayerService(repo: PlayerRepository) {


  def register(reg: Register): Future[Int] = {
    val player = Player(0, reg.username, reg.password, UserType.User, Balance(100))
    repo.insert(player)
  }

}
