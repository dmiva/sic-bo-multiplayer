package com.dmiva.sicbo.actors

import akka.actor.{Actor, Props}
import com.dmiva.sicbo.actors
import com.dmiva.sicbo.actors.repository.PlayerRepository
import com.dmiva.sicbo.common.IncomingMessage

object Lobby {
  def props() = Props(new Lobby())
}

// a.k.a API Gateway
class Lobby extends Actor {
  private val gameRoom = context.actorOf(GameRoom.props())
  private val playerRepository = context.actorOf(PlayerRepository.props())

  override def receive: Receive = {
    case msg: PlayerRepository.Command.Register  => playerRepository forward msg
    case msg: PlayerRepository.Command.Login => playerRepository forward msg

    case msg: IncomingMessage.PlaceBet  => gameRoom forward msg
    case msg: GameRoom.Join             => gameRoom forward msg
    case msg: GameRoom.Leave            => gameRoom forward msg
  }
}
