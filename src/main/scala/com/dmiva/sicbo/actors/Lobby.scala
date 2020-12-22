package com.dmiva.sicbo.actors

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, OneForOneStrategy, Props}
import com.dmiva.sicbo.service.PlayerService

import scala.concurrent.duration.DurationInt

object Lobby {
  def props(service: PlayerService): Props = Props(new Lobby(service: PlayerService))
}

class Lobby(service: PlayerService) extends Actor {
  private val gameRoom = context.actorOf(GameRoom.props(), "game")
  private val userService = context.actorOf(UserService.props(service), "userService")

  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 2, withinTimeRange = 4.seconds) {
      case _: Exception                => Restart
    }

  override def receive: Receive = {
    case msg: UserService.Command.Login          => userService forward msg
    case msg: UserService.Command.UpdateBalance  => userService forward msg

    case msg: GameRoom.Command.PlaceBet          => gameRoom forward msg
    case msg: GameRoom.Command.Join              => gameRoom forward msg
    case msg: GameRoom.Command.Leave             => gameRoom forward msg
  }
}

