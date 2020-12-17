package com.dmiva.sicbo.domain

import com.dmiva.sicbo.common.BetRejectReason
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import com.dmiva.sicbo.domain.Player.{Balance, Player, UserType}
import io.circe.Decoder.state

class GameStateSpec extends AnyFunSuite with Matchers {

  test("Should be able to place bet") {

    val initialBalance = BigDecimal(100)
    val betAmount = BigDecimal(50)
    val player = Player(0, "name", "pass", UserType.User, Balance(initialBalance))
    val bets = List(
      Bet(Some(betAmount), BetType.Total(18), None)
    )

    val gameState = GameState
      .initGame
      .startNewGame
      .addPlayer(player)
      .placeBet(player.username, bets)

    val placedBetsAmount = gameState match {
      case Right(state) => state.numberOfBetsPlaced(player.username)
      case Left(e) => 0
    }

    val balanceAfterPlacedBets = gameState match {
      case Right(state) => state.getPlayerBalance(player.username)
      case Left(e) => initialBalance
    }

    placedBetsAmount shouldEqual bets.length
    balanceAfterPlacedBets shouldEqual (initialBalance - betAmount)
  }

  test("Should not be able to place bet in other game phases") {
    val game: GameState = GameState.initGame.setGamePhase(GamePhase.RollingDice)

    val player = Player(0, "name", "pass", UserType.User, Balance(100))
    val bets = List(
      Bet(Some(20), BetType.Total(5), None),
      Bet(Some(50), BetType.Total(18), None)
    )

    val newGameState = game.placeBet(player.username, bets)

    val placedBetsAmount = newGameState match {
      case Right(state) => state.numberOfBetsPlaced(player.username)
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

  test("Calculate game results with betting #1") {
    val game: GameState = GameState.initGame.setGamePhase(GamePhase.PlacingBets)
    val player = Player(0, "name", "pass", UserType.User, Balance(100))

    val dice = DiceOutcome(1,3,5)
    val bets = List(
      Bet(Some(20), BetType.Number(5), None), // win = 40
      Bet(Some(50), BetType.Total(9), None) // win = 350
    )

    val result = for {
      a <- game.addPlayer(player).placeBet(player.username, bets)
      b <- a.setGamePhase(GamePhase.RollingDice).applyDiceOutcome(dice)
      newState <- b.setGamePhase(GamePhase.MakePayouts).calculateResults
    } yield newState.gameResult

    val (totalWin, newBalance) = result match {
      case Left(_) => (BigDecimal(0), BigDecimal(0))
      case Right(map) => map.get(player.username) match {
        case Some(value) => (value.totalWin, value.balance.amount)
        case None => (BigDecimal(0), BigDecimal(0))
      }
    }
    totalWin shouldEqual 390 // 350 + 40
    newBalance shouldEqual 420 // 100 + 390 - 70
  }

  test("Calculate game results with betting #2") {

    val initialBalance = BigDecimal(1000)
    val betAmount = BigDecimal(50)

    val player: Player = Player(0, "Player1", "pw", UserType.User, Balance(initialBalance))

    val bets: List[Bet] = List(
      Bet(Some(betAmount), BetType.Total(8), None), // win = 0
      Bet(Some(betAmount), BetType.Double(3), None), // win = 450
      Bet(Some(betAmount), BetType.Even, None) // win = 100
    )

    val dice = DiceOutcome(3,3,6)

    val result = for {
      a <- GameState.initGame.startNewGame.addPlayer(player).placeBet(player.username, bets)
      b <- a.setGamePhase(GamePhase.RollingDice).applyDiceOutcome(dice)
      newState <- b.setGamePhase(GamePhase.MakePayouts).calculateResults
    } yield newState.gameResult


    val (totalWin, newBalance) = result match {
      case Left(_) => (BigDecimal(0), BigDecimal(0))
      case Right(map) => map.get(player.username) match {
        case Some(value) => (value.totalWin, value.balance.amount)
        case None => (BigDecimal(0), BigDecimal(0))
      }
    }

    val expectedWin = BigDecimal(550)

    totalWin shouldEqual expectedWin
    newBalance shouldEqual (initialBalance + expectedWin - (betAmount * bets.length))
  }

  test("Calculate game results without betting") {

    val initialBalance = BigDecimal(1000)
    val betAmount = BigDecimal(50)

    val player: Player = Player(0, "Player1", "pw", UserType.User, Balance(initialBalance))

    val bets: List[Bet] = List.empty

    val dice = DiceOutcome(3,3,6)

    val result = for {
      a <- GameState.initGame
        .startNewGame
        .addPlayer(player)
        .placeBet(player.username, bets)
      b <- a
        .setGamePhase(GamePhase.RollingDice)
        .applyDiceOutcome(dice)
      newState <- b
        .setGamePhase(GamePhase.MakePayouts)
        .calculateResults
    } yield newState.gameResult


    val (totalWin, newBalance) = result match {
      case Left(_) => (BigDecimal(0), BigDecimal(0))
      case Right(map) => map.get(player.username) match {
        case Some(value) => (value.totalWin, value.balance.amount)
        case None => (BigDecimal(0), BigDecimal(0))
      }
    }

    val expectedWin = BigDecimal(0)

    totalWin shouldEqual expectedWin
    newBalance shouldEqual (initialBalance + expectedWin - (betAmount * bets.length))
  }

  test("Handle when players enters room, places bet, and leaves the room") {

    val initialBalance = BigDecimal(1000)
    val betAmount = BigDecimal(50)

    val player: Player = Player(0, "Player1", "pw", UserType.User, Balance(initialBalance))

    val bets: List[Bet] = List(
      Bet(Some(betAmount), BetType.Total(8), None), // win = 0
      Bet(Some(betAmount), BetType.Double(3), None), // win = 450
      Bet(Some(betAmount), BetType.Even, None) // win = 100
    )

    val dice = DiceOutcome(3,3,6)

    val result = for {
      a <- GameState
        .initGame
        .startNewGame
        .addPlayer(player) // player enters the game
        .placeBet(player.username, bets)
      b <- a
        .setGamePhase(GamePhase.RollingDice)
        .removePlayer(player) // player leaves the game
        .applyDiceOutcome(dice)
      newState <- b
        .setGamePhase(GamePhase.MakePayouts)
        .calculateResults
    } yield newState.gameResult


    val (totalWin, newBalance) = result match {
      case Left(_) => (BigDecimal(0), BigDecimal(0))
      case Right(map) => map.get(player.username) match {
        case Some(value) => (value.totalWin, value.balance.amount)
        case None => (BigDecimal(0), BigDecimal(0))
      }
    }

    val expectedWin = BigDecimal(550)

    totalWin shouldEqual expectedWin
    newBalance shouldEqual (initialBalance + expectedWin - (betAmount * bets.length))
  }

}
