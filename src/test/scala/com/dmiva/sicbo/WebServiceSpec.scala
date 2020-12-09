package com.dmiva.sicbo

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class WebServiceSpec extends AnyFunSuite with Matchers with ScalatestRouteTest {

  val server = new WebService()
//  val wsClient: WSProbe = WSProbe()
  val gameUri = "/game"

  val testName1 = "Hacker777"
  val testName2 = "' or '1'='1"


  test("Webserver should respond to root path request") {
    Get() ~> server.routes ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

  test("Websocket should respond to first login request with success") {
    val wsClient = WSProbe()
    WS(gameUri, wsClient.flow) ~> server.routes ~>
      check {
        wsClient.sendMessage(Requests.Login(testName1))
        wsClient.expectMessage(Responses.LoginSuccessful)

        wsClient.sendCompletion()
        wsClient.expectCompletion()
      }
  }

  test("Websocket should respond with error to the second login request") {
    val wsClient = WSProbe()
    WS(gameUri, wsClient.flow) ~> server.routes ~>
      check {
        wsClient.sendMessage(Requests.Login(testName1))
        wsClient.expectMessage(Responses.LoginSuccessful)

        wsClient.sendMessage(Requests.Login(testName1))
        wsClient.expectMessage(Responses.ErrorAlreadyLoggedIn)

        wsClient.sendCompletion()
        wsClient.expectCompletion()
      }
  }

  test("Websocket should respond with correct message to logout request when user is logged in") {
    val wsClient = WSProbe()
    WS(gameUri, wsClient.flow) ~> server.routes ~>
      check {
        wsClient.sendMessage(Requests.Login(testName1))
        wsClient.expectMessage(Responses.LoginSuccessful)

        wsClient.sendMessage(Requests.Logout(testName1))
        wsClient.expectMessage(Responses.LoggedOut)

        wsClient.sendCompletion()
        wsClient.expectCompletion()
      }
  }

  test("Websocket should respond with error message to logout request when user is not logged in") {
    val wsClient = WSProbe()
    WS(gameUri, wsClient.flow) ~> server.routes ~>
      check {
        wsClient.sendMessage(Requests.Logout(testName1))
        wsClient.expectMessage(Responses.ErrorNotLoggedIn)

        wsClient.sendCompletion()
        wsClient.expectCompletion()
      }
  }

}
