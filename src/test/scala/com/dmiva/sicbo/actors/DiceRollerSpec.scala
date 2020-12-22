package com.dmiva.sicbo.actors

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.{HttpResponse, Uri}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class DiceRollerSpec
  extends TestKit(ActorSystem("DiceRollerSpec"))
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "DiceRoller actor" should {

    "return a dice gameResult in normal conditions" in {
      val probe = TestProbe()
      val diceRoller = probe.childActorOf(DiceRoller.props())

      probe.send(diceRoller, DiceRoller.RollDice)
      probe.expectMsgType[DiceRoller.DiceResult]
    }

    "return a dice gameResult if True RNG is returning back expected gameResult" in {
      val minNum = 1
      val maxNum = 6
      val httpUrl = s"https://www.random.org/integers/?num=3&min=$minNum&max=$maxNum&col=1&base=10&format=plain&rnd=new"
      val probe = TestProbe()
      val diceRoller = system.actorOf(Props(new DiceRoller() {
        // overring input parameter with other url
        override def getTrueRandomNumber(uri: Uri): Future[HttpResponse] =
          super.getTrueRandomNumber(httpUrl)
      }))

      probe.send(diceRoller, DiceRoller.RollDice)
      probe.expectMsgType[DiceRoller.DiceResult]
    }

    "return a dice gameResult if True RNG is returning back unexpected gameResult" in {
      val minNum = 10
      val maxNum = 20
      val httpUrl = s"https://www.random.org/integers/?num=3&min=$minNum&max=$maxNum&col=1&base=10&format=plain&rnd=new"
      val probe = TestProbe()
      val diceRoller = system.actorOf(Props(new DiceRoller() {
        // overring input parameter with other url
        override def getTrueRandomNumber(uri: Uri): Future[HttpResponse] =
          super.getTrueRandomNumber(httpUrl)
      }))

      probe.send(diceRoller, DiceRoller.RollDice)
      probe.expectMsgType[DiceRoller.DiceResult]
    }

    "return a dice gameResult if True RNG HTTP provider is an unknown host" in {
      val httpUrl = "https://www.random.org123"
      val probe = TestProbe()
      val diceRoller = system.actorOf(Props(new DiceRoller() {
        // overring input parameter with other url
        override def getTrueRandomNumber(uri: Uri): Future[HttpResponse] =
          super.getTrueRandomNumber(httpUrl)
      }))
      within(7.seconds) {
        probe.send(diceRoller, DiceRoller.RollDice)
        probe.expectMsgType[DiceRoller.DiceResult]

      }
    }

    // TODO: Other tests

  }
}
