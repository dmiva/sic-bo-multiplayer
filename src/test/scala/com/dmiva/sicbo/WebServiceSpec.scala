package com.dmiva.sicbo

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class WebServerSpec extends AnyFunSuite with Matchers with ScalatestRouteTest {
  test("Webserver should respond to root path request") {
    val server = new WebService()
    Get() ~> server.routes ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

  test("Websocket should respond to first login request with success") {
    val server = new WebService()
    val wsClient = WSProbe()
    WS("/game", wsClient.flow) ~> server.routes ~>
      check {
        wsClient.sendMessage("{\n  \"$type\" : \"login\",\n  \"username\" : \"John\"\n}")
        wsClient.expectMessage("{\n  \"$type\" : \"login_successful\"\n}")
        wsClient.sendCompletion()
        wsClient.expectCompletion()
      }
  }

  test("Websocket should respond with error to the second login request") {
    val server = new WebService()
    val wsClient = WSProbe()
    WS("/game", wsClient.flow) ~> server.routes ~>
      check {
        wsClient.sendMessage("{\n  \"$type\" : \"login\",\n  \"username\" : \"John\"\n}")
        wsClient.expectMessage("{\n  \"$type\" : \"login_successful\"\n}")
        wsClient.sendMessage("{\n  \"$type\" : \"login\",\n  \"username\" : \"John\"\n}")
        wsClient.expectMessage("{\n  \"text\" : \"Login attempt when already logged in\",\n  \"$type\" : \"error\"\n}")
        wsClient.sendCompletion()
        wsClient.expectCompletion()
      }
  }

}
