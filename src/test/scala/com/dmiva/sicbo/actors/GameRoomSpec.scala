package com.dmiva.sicbo.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.dmiva.sicbo.common.OutgoingMessage.GamePhaseChanged
import com.dmiva.sicbo.domain
import com.dmiva.sicbo.domain.{Balance, GamePhase, Player, PlayerSession, UserType}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

class GameRoomSpec extends TestKit(ActorSystem("GameRoomSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

//  "GameRoom actor" must {
//
//    "start a game when player joins" in {
//      val probe = TestProbe()
//      val actor = system.actorOf(Props(new GameRoom()))
//      val session = PlayerSession(domain.Player(0, "Name", "Pass", UserType.User, Balance(100)), probe.ref)
//
//      actor ! GameRoom.Join(probe.ref, session)
//
//    }
//  }

}
