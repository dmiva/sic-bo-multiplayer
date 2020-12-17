package com.dmiva.sicbo.actors.repository

import com.dmiva.sicbo.actors.repository.PlayerRepository.Event
import com.dmiva.sicbo.domain.{Name, Player}


case class PlayerStorage(players: Map[Name, Player] = Map.empty) extends CborSerializable {

  def getPlayerByName(username: Name): Option[Player] = {
    players.get(username)
  }

  def isPlayerRegistered(username: Name): Boolean = {
    players.contains(username)
  }

  def updated(event: Event): PlayerStorage = {
    event match {
      case Event.Registered(name, player) => this.copy(players = players + (name -> player))
      case Event.BalanceUpdated(name, player) => this.copy(players = players + (name -> player))
    }
  }

}
