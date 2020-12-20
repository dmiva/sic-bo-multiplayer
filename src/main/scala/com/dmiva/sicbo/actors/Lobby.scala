package com.dmiva.sicbo.actors

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{Actor, ActorKilledException, OneForOneStrategy, Props}
import com.dmiva.sicbo.actors.repository.PlayerRepository
import scala.concurrent.duration.DurationInt

object Lobby {
  def props() = Props(new Lobby())
}

// a.k.a API Gateway
class Lobby extends Actor {
  private val gameRoom = context.actorOf(GameRoom.props(), "game")
  private val playerRepository = context.actorOf(PlayerRepository.props(), "repository")

  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 2, withinTimeRange = 4.seconds) {
      case _: ActorKilledException     => Restart
      case _: Exception                => Restart
    }

  override def receive: Receive = {
    case msg: PlayerRepository.Command.Register       => playerRepository forward msg
    case msg: PlayerRepository.Command.Login          => playerRepository forward msg
    case msg: PlayerRepository.Command.UpdateBalance  => playerRepository forward msg

    case msg: GameRoom.Command.PlaceBet               => gameRoom forward msg
    case msg: GameRoom.Command.Join                   => gameRoom forward msg
    case msg: GameRoom.Command.Leave                  => gameRoom forward msg
  }
}

