package com.dmiva.sicbo.actors

import akka.actor.{Actor, Props}
import com.dmiva.sicbo.actors.repository.PlayerRepository

object Lobby {
  def props() = Props(new Lobby())
}

// a.k.a API Gateway
class Lobby extends Actor {
//  private val gameRoom = context.actorOf(GameRoom.props(), "game")
  private val gameRoom = context.actorOf(GameRoomPers.props(), "game")
  private val playerRepository = context.actorOf(PlayerRepository.props(), "repository")

  // TODO: Add supervision
  override def receive: Receive = {
    case msg: PlayerRepository.Command.Register       => playerRepository forward msg
    case msg: PlayerRepository.Command.Login          => playerRepository forward msg
    case msg: PlayerRepository.Command.UpdateBalance  => playerRepository forward msg

//    case msg: GameRoom.PlaceBet                       => gameRoom forward msg
//    case msg: GameRoom.Join                           => gameRoom forward msg
//    case msg: GameRoom.Leave                          => gameRoom forward msg
    case msg: GameRoomPers.Command.PlaceBet                       => gameRoom forward msg
    case msg: GameRoomPers.Command.Join                           => gameRoom forward msg
    case msg: GameRoomPers.Command.Leave                          => gameRoom forward msg
  }
}

