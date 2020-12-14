package com.dmiva.sicbo.actors

import akka.actor.{Actor, Props}
import com.dmiva.sicbo.actors
import com.dmiva.sicbo.actors.repository.UserRepository
import com.dmiva.sicbo.common.IncomingMessage

object Lobby {
  def props() = Props(new Lobby())
}

// a.k.a API Gateway
class Lobby extends Actor {
  private val gameRoom = context.actorOf(GameRoom.props())
  private val userRepository = context.actorOf(UserRepository.props())

  override def receive: Receive = {
    case msg: UserRepository.Command.Register  => userRepository forward msg
    case msg: UserRepository.Command.Login => userRepository forward msg

    case msg: IncomingMessage.PlaceBet  => gameRoom forward msg
    case msg: GameRoom.Join             => gameRoom forward msg
    case msg: GameRoom.Leave            => gameRoom forward msg
  }
}
