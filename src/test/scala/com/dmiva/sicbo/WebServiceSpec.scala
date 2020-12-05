package com.dmiva.sicbo

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class WebServerSpec extends AnyFunSuite with Matchers with ScalatestRouteTest {
  test("Webserver should respond to root path request") {
    val server = new WebService()
    Get() ~> server.route ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

  test("Websocket should respond with echo") {
    val server = new WebService()
    val wsClient = WSProbe()
    WS("/game", wsClient.flow) ~> server.webSocketRoute ~>
      check {
        wsClient.sendMessage("Hello")
        wsClient.expectMessage("Hello")
        wsClient.sendCompletion()
        wsClient.expectCompletion()
      }
  }

}
