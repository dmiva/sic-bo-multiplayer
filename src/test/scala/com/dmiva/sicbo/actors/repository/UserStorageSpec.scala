package com.dmiva.sicbo.actors.repository

import com.dmiva.sicbo.domain.Player.{User, UserType}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class UserStorageSpec extends AnyFunSuite with Matchers {

  val nameThatExists = "Name_4"
  val userThatExists: User = User(4, nameThatExists, "Password_4", UserType.User)
  val nameThatDontExist = "Name_1123"

  def populateStorage(storage: UserStorage, numOfUsers: Int): UserStorage = {
    val name = s"Name_$numOfUsers"
    val user = User(numOfUsers, name, s"Password_$numOfUsers", UserType.User)
    if (numOfUsers > 0) populateStorage(storage.copy(storage.users + (name -> user)), numOfUsers - 1)
    else storage
  }

  test("User storage should append users") {
    val storage = populateStorage(UserStorage(), 10)
    val name1 = "NewUser"
    val user1 = User(123, name1, "NewPass", UserType.User)
    val event1 = UserRepository.Event.Registered(name1, user1)

    storage.users.size shouldEqual 10
    val newStorage = storage.updated(event1)
    newStorage.users.size shouldEqual 11
  }

  test("User storage should return Some(User(...)) with getUserByName if user exists") {
    val storage = populateStorage(UserStorage(), 10)
    storage.getUserByName(nameThatExists) shouldEqual Some(userThatExists)
  }

  test("User storage should return None with getUserByName if user don't exists") {
    val storage = populateStorage(UserStorage(), 10)
    storage.getUserByName(nameThatDontExist) shouldEqual None
  }

  test("User storage should return true if user is registered") {
    val storage = populateStorage(UserStorage(), 10)
    storage.isUserRegistered(nameThatExists) shouldEqual true
  }

  test("User storage should return false if user is not registered") {
    val storage = populateStorage(UserStorage(), 10)
    storage.isUserRegistered(nameThatDontExist) shouldEqual false
  }

}
