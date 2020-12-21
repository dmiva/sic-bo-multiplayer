package com.dmiva.sicbo

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import com.dmiva.sicbo.actors.Lobby
import com.dmiva.sicbo.common.IncomingMessage.Register
import com.dmiva.sicbo.common.codecs.IncomingMessageCodecs.incomingMessageOps
import com.dmiva.sicbo.domain.{Balance, Bet, BetType, GamePhase, PlayerInfo, UserType}
import com.dmiva.sicbo.repository.PlayerRepository
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import com.dmiva.sicbo.service.PlayerService
import org.mockito.MockitoSugar

class HttpRoutesSpec extends AnyFunSuite with Matchers with MockitoSugar with ScalatestRouteTest {

//  val server = new HttpRoutes()
//  val wsClient: WSProbe = WSProbe()
  val gameUri = "/game"
  val db = mock[PlayerRepository]
  val service = new PlayerService(db)
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

  val lobby = system.actorOf(Lobby.props())

  test("HTTP should response to /register endpoint ") {
    val registrationRequest = Register("Name", "pass").toText

    val request = Post("/register").withEntity(ContentTypes.`application/json`, registrationRequest)
    println(registrationRequest)
    val server = new HttpRoutes(lobby, service)
    request ~> server.routes ~> check {
      status shouldEqual StatusCodes.Created
    }
  }

  test("Webserver should respond to root path request") {
    val server = new HttpRoutes(lobby, service)
    Get() ~> server.routes ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

  test("Websocket should respond to first login request with success") {
    val wsServer = new HttpRoutes(lobby, service)
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
    val wsServer = new HttpRoutes(lobby, service)
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
    val wsServer = new HttpRoutes(lobby, service)
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
    val wsServer = new HttpRoutes(lobby, service)
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
    val wsServer = new HttpRoutes(lobby, service)
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
    val wsServer = new HttpRoutes(lobby, service)
    val wsClient = WSProbe()
    WS(gameUri, wsClient.flow) ~> wsServer.routes ~>
      check {
//        wsClient.sendMessage(Requests.Register(testName1, testPass1))
//        wsClient.expectMessage(Responses.RegistrationSuccessful)

        val player = PlayerInfo(0, testName1, userType, Balance(100))

        wsClient.sendMessage(Requests.Login(testName1, testPass1))
        wsClient.expectMessage(Responses.LoginSuccessful(player))

        wsClient.expectMessage(Responses.GamePhaseChanged(GamePhase.PlacingBets))
        wsClient.sendMessage(Requests.PlaceBet(testBets))
//                Requests.PlaceBet(testBets) shouldEqual "asd"
        wsClient.expectMessage(Responses.BetAccepted)

        wsClient.sendCompletion()
        wsClient.expectCompletion()
      }
  }
}
