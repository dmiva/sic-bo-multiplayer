package com.dmiva.sicbo.actors.repository

import com.dmiva.sicbo.domain.Player.{Name, User}
import com.dmiva.sicbo.actors.repository.UserRepository.Event


case class UserStorage(users: Map[Name, User] = Map.empty) {

  def getUserByName(username: Name): Option[User] = {
    users.get(username)
  }

  def isUserRegistered(username: Name): Boolean = {
    users.contains(username)
  }

  def updated(event: Event): UserStorage = {
    event match {
      case Event.Registered(name, user) => this.copy(users = users + (name -> user))
    }
  }

}
