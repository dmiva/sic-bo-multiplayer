package com.dmiva.sicbo

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import com.dmiva.sicbo.domain.{Balance, Bet, BetType, GamePhase, PlayerInfo, UserType}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class WebServiceSpec extends AnyFunSuite with Matchers with ScalatestRouteTest {

//  val server = new WebService()
//  val wsClient: WSProbe = WSProbe()
  val gameUri = "/game"

  val userType = UserType.User
  val testName1 = "Hacker777"
  val testPass1 = "Password123"
  val testName2 = "' or '1'='1"
//  val testBets = List(
//    Bet(Some(50), BetType.Combo(2,5), None),
//    Bet(Some(30), BetType.Total(6), None)
//  )

  val testBets = List(
    Bet(Some(30), BetType.Total(6), None)
  )


  test("Webserver should respond to root path request") {
    val server = new WebService()
    Get() ~> server.routes ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

  test("Websocket should respond to first login request with success") {
    val wsServer = new WebService()
    val wsClient = WSProbe()
    WS(gameUri, wsClient.flow) ~> wsServer.routes ~>
      check {
        wsClient.sendMessage(Requests.Register(testName1, testPass1))
        wsClient.expectMessage(Responses.RegistrationSuccessful)

        val player = PlayerInfo(0, testName1, userType, Balance(100))

        wsClient.sendMessage(Requests.Login(testName1, testPass1))
        wsClient.expectMessage(Responses.LoginSuccessful(player))

        wsClient.sendCompletion()
        wsClient.expectCompletion()
      }
  }

  test("Websocket should respond with error to the second login request") {
    val wsServer = new WebService()
    val wsClient = WSProbe()
    WS(gameUri, wsClient.flow) ~> wsServer.routes ~>
      check {
//        wsClient1.sendMessage(Requests.Register(testName1, testPass1))
//        wsClient1.expectMessage(Responses.RegistrationSuccessful)

        val player = PlayerInfo(0, testName1, userType, Balance(100))

        wsClient.sendMessage(Requests.Login(testName1, testPass1))
        wsClient.expectMessage(Responses.LoginSuccessful(player))

        wsClient.sendMessage(Requests.Login(testName1, testPass1))
        wsClient.expectMessage(Responses.ErrorAlreadyLoggedIn)

        wsClient.sendCompletion()
        wsClient.expectCompletion()
      }
  }

  test("Websocket should respond with correct message to logout request when user is logged in") {
    val wsServer = new WebService()
    val wsClient = WSProbe()
    WS(gameUri, wsClient.flow) ~> wsServer.routes ~>
      check {
//        wsClient.sendMessage(Requests.Register(testName1, testPass1))
//        wsClient.expectMessage(Responses.RegistrationSuccessful)

        val player = PlayerInfo(0, testName1, userType, Balance(100))

        wsClient.sendMessage(Requests.Login(testName1, testPass1))
        wsClient.expectMessage(Responses.LoginSuccessful(player))

        wsClient.sendMessage(Requests.Logout(testName1))
        wsClient.expectMessage(Responses.LoggedOut)

        wsClient.sendCompletion()
        wsClient.expectCompletion()
      }
  }

  test("Websocket should respond with error message to logout request when user is not logged in") {
    val wsServer = new WebService()
    val wsClient = WSProbe()
    WS(gameUri, wsClient.flow) ~> wsServer.routes ~>
      check {
//        wsClient.sendMessage(Requests.Register(testName1, testPass1))
//        wsClient.expectMessage(Responses.RegistrationSuccessful)

        wsClient.sendMessage(Requests.Logout(testName1))
        wsClient.expectMessage(Responses.ErrorInvalidRequest)

        wsClient.sendCompletion()
        wsClient.expectCompletion()
      }
  }

  test("Websocket should respond with error message to place bet request when user is not logged in") {
    val wsServer = new WebService()
    val wsClient = WSProbe()
    WS(gameUri, wsClient.flow) ~> wsServer.routes ~>
      check {
//        wsClient.sendMessage(Requests.Register(testName1, testPass1))
//        wsClient.expectMessage(Responses.RegistrationSuccessful)

        wsClient.sendMessage(Requests.PlaceBet(testBets))
//        Requests.PlaceBet(testBets) shouldEqual "asd"
        wsClient.expectMessage(Responses.ErrorInvalidRequest)

        wsClient.sendCompletion()
        wsClient.expectCompletion()
      }
  }

  test("Websocket should accept message to place bet request when is logged in") {
    val wsServer = new WebService()
    val wsClient = WSProbe()
    WS(gameUri, wsClient.flow) ~> wsServer.routes ~>
      check {
//        wsClient.sendMessage(Requests.Register(testName1, testPass1))
//        wsClient.expectMessage(Responses.RegistrationSuccessful)

        val player = PlayerInfo(0, testName1, userType, Balance(100))

        wsClient.sendMessage(Requests.Login(testName1, testPass1))
        wsClient.expectMessage(Responses.LoginSuccessful(player))
        Thread.sleep(4000)
        wsClient.expectMessage(Responses.GamePhaseChanged(GamePhase.PlacingBets))
        wsClient.sendMessage(Requests.PlaceBet(testBets))
//                Requests.PlaceBet(testBets) shouldEqual "asd"
        wsClient.expectMessage(Responses.BetAccepted)

        wsClient.sendCompletion()
        wsClient.expectCompletion()
      }
  }
}
