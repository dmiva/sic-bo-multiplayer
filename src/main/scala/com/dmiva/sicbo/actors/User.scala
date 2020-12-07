package com.dmiva.sicbo.actors

import akka.actor.Actor

object User {

}

class User(name: String) extends Actor {
  override def receive: Receive = ???
}
