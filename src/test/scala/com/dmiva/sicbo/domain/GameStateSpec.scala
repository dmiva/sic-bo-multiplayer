package com.dmiva.sicbo.domain

import com.dmiva.sicbo.common.BetRejectReason
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import com.dmiva.sicbo.domain.Player.{Balance, Player, UserType}
import io.circe.Decoder.state

class GameStateSpec extends AnyFunSuite with Matchers {


  test("should be able to place bet") {
    val game: GameState = GameState.initGame.startNewGame
    val player = Player(0, "name", "pass", UserType.User, Balance(100))
    val bets = List(
      Bet(Some(20), BetType.Total(5), None),
      Bet(Some(50), BetType.Total(18), None)
    )

    val newGameState = game.placeBet(player, bets)

    val placedBetsAmount = newGameState match {
      case Right(state) => state.numberOfBetsPlaced(player)
      case Left(_) => 0
    }

    placedBetsAmount shouldEqual 2
  }

  test("Should not be able to place bet in other game phases") {
    val game: GameState = GameState.initGame.setGamePhase(GamePhase.RollingDice)

    val player = Player(0, "name", "pass", UserType.User, Balance(100))
    val bets = List(
      Bet(Some(20), BetType.Total(5), None),
      Bet(Some(50), BetType.Total(18), None)
    )

    val newGameState = game.placeBet(player, bets)

    val placedBetsAmount = newGameState match {
      case Right(state) => state.numberOfBetsPlaced(player)
      case Left(_) => 0
    }

    newGameState shouldEqual Left(BetRejectReason.TimeExpired)
    placedBetsAmount shouldEqual 0
  }

  test("Should be able to apply dice outcome") {
    val game: GameState = GameState.initGame.setGamePhase(GamePhase.RollingDice)
    val player = Player(0, "name", "pass", UserType.User, Balance(100))

    val bets = List(
      Bet(Some(20), BetType.Total(5), None),
      Bet(Some(50), BetType.Total(18), None)
    )
    val dice = DiceOutcome(1,3,5)
    val newGameState = game.applyDiceOutcome(dice)

    newGameState match {
      case Right(state) => newGameState shouldEqual Right(state)
      case Left(_) => 0 // TODO: Ugly
    }

  }

  test("Should not be able to apply dice outcome in incorrect game phase") {
    val game: GameState = GameState.initGame.setGamePhase(GamePhase.PlacingBets)
    val player = Player(0, "name", "pass", UserType.User, Balance(100))

    val bets = List(
      Bet(Some(20), BetType.Total(5), None),
      Bet(Some(50), BetType.Total(18), None)
    )
    val dice = DiceOutcome(1,3,5)
    val newGameState = game.applyDiceOutcome(dice)

    newGameState shouldEqual Left("Invalid game state")
  }

  test("Should be able to calculate game results") {
    val game: GameState = GameState.initGame.setGamePhase(GamePhase.PlacingBets)
    val player = Player(0, "name", "pass", UserType.User, Balance(100))

    val dice = DiceOutcome(1,3,5)
    val bets = List(
      Bet(Some(20), BetType.Number(5), None), // win = 40
      Bet(Some(50), BetType.Total(9), None) // win = 350
    )

    val result = for {
      a <- game.placeBet(player, bets)
      b <- a.setGamePhase(GamePhase.RollingDice).applyDiceOutcome(dice)
      newState <- b.setGamePhase(GamePhase.MakePayouts).calculateResults
    } yield newState.gameResult

    val (totalWin, newBalance) = result match {
      case Left(_) => (0, 0)
      case Right(map) => map.get(player) match {
        case Some(value) => (value.totalWin, value.playerInfo.balance.amount)
        case None => (0, 0)
      }
    }
    totalWin shouldEqual 390 // 350 + 40
    newBalance shouldEqual 420 // 100 + 390 - 70
  }

}
