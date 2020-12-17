package com.dmiva.sicbo.actors.repository

import com.dmiva.sicbo.domain.Player.{Balance, Player, UserType}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class PlayerStorageSpec extends AnyFunSuite with Matchers {

  val userType: UserType = UserType.User
  val nameThatExists = "Name_4"
  val userThatExists: Player = Player(4, nameThatExists, "Password_4", userType, Balance(0))
  val nameThatDontExist = "Name_1123"

  def populateStorage(storage: PlayerStorage, numOfUsers: Int): PlayerStorage = {
    val name = s"Name_$numOfUsers"
    val user = Player(numOfUsers, name, s"Password_$numOfUsers", userType, Balance(0))
    if (numOfUsers > 0) populateStorage(storage.copy(storage.players + (name -> user)), numOfUsers - 1)
    else storage
  }

  test("User storage should append users") {
    val storage = populateStorage(PlayerStorage(), 10)
    val name1 = "NewUser"
    val user1 = Player(123, name1, "NewPass", userType, Balance(0))
    val event1 = PlayerRepository.Event.Registered(name1, user1)

    storage.players.size shouldEqual 10
    val newStorage = storage.updated(event1)
    newStorage.players.size shouldEqual 11
  }

  test("User storage should return Some(User(...)) with getUserByName if user exists") {
    val storage = populateStorage(PlayerStorage(), 10)
    storage.getPlayerByName(nameThatExists) shouldEqual Some(userThatExists)
  }

  test("User storage should return None with getUserByName if user don't exists") {
    val storage = populateStorage(PlayerStorage(), 10)
    storage.getPlayerByName(nameThatDontExist) shouldEqual None
  }

  test("User storage should return true if user is registered") {
    val storage = populateStorage(PlayerStorage(), 10)
    storage.isPlayerRegistered(nameThatExists) shouldEqual true
  }

  test("User storage should return false if user is not registered") {
    val storage = populateStorage(PlayerStorage(), 10)
    storage.isPlayerRegistered(nameThatDontExist) shouldEqual false
  }

}
