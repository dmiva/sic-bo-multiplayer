package com.dmiva.sicbo.domain

import com.dmiva.sicbo.actors.repository.CborSerializable
import com.dmiva.sicbo.common.BetRejectReason
import com.dmiva.sicbo.common.OutgoingMessage.GameResult


object GameState {

  def initGame: GameState = GameState(
    phase = GamePhase.Idle,
    players = Map.empty,
    playersBets = Map.empty,
    dice = None,
    gameResult = Map.empty
  )
}
final case class PlayerState(name: Name, balance: Balance, quit: Boolean) extends CborSerializable

final case class GameState(
                            phase: GamePhase,
                            players: Map[Name, PlayerState],
                            playersBets: Map[Name, List[Bet]],
                            dice: Option[DiceOutcome],
                            gameResult: Map[Name, GameResult]
                          ) extends CborSerializable {

  /**
   * Starts new game session. All bets and payout are cleaned.
   * Players balances are retained until they leave the game.
   * Players that have previously left game, are removed.
   */
  def startNewGame: GameState = this.copy(
    phase = GamePhase.PlacingBets,
    players = players.filterNot{ case (_, playerState) => playerState.quit},
    playersBets = Map.empty,
    dice = None,
    gameResult = Map.empty
  )

  def addPlayer(player: Player): GameState = {
    this.copy(players = players + (player.username -> PlayerState(player.username, player.balance, quit = false)))
  }

  /** When player leaves the game, it's entry is only marked with flag. To be cleaned at the start of next game */
  def removePlayer(player: Player): GameState = {
    players.get(player.username) match {
      case Some(playerState) =>
        this.copy(players = players + (player.username -> playerState.copy(quit = true)))
      case None => // player that has already left, don't affect the game
        this.copy(players = players - player.username)
    }
  }

  /** If player is in game and has enough balance, then bet is accepted */
  def placeBet(name: Name, bets: List[Bet]): Either[String, GameState] = {
    phase match {
      case GamePhase.PlacingBets =>
        val betListEmpty = bets.isEmpty // TODO: Refactor this validation
        val pendingBetAmount = bets.map(_.amount.getOrElse(BigDecimal(0))).sum
        val invalidBetAmount = bets.exists(b => b.amount <= Some(0) || b.amount.isEmpty)
        val currentBalance = getPlayerBalance(name)
        if (betListEmpty)
          Left(BetRejectReason.MustBeAtLeastOneBet)
        else
          if (invalidBetAmount)
            Left(BetRejectReason.BetAmountNotPositive)
          else
            if (currentBalance >= pendingBetAmount)
              Right(this.copy(
                players = players + (name -> PlayerState(name, Balance(currentBalance - pendingBetAmount), quit = false)),
                playersBets = playersBets + (name -> playersBets.getOrElse(name, List.empty).concat(bets))))
            else
              Left(BetRejectReason.NotEnoughBalance)
      case _ => Left(BetRejectReason.TimeExpired)
    }
  }

  def applyDiceOutcome(dice: DiceOutcome): Either[String, GameState] = {
    phase match {
      case GamePhase.RollingDice =>
        Right(this.copy(dice = Some(dice)))
      case _ => Left("Invalid game state")
    }
  }

  /** Results are prepared for all players that were in room, whether they did anything or not */
  def calculateResults: Either[String, GameState] = {
    phase match {
      case GamePhase.MakePayouts =>
        dice match {
          case None => Left("Dice value is not known")
          case Some(diceOutcome) =>
            val paytable = Paytable(diceOutcome)
            val winningBetTypes = BetType.allBets.filter(b => Bet.isWinningBetType(b, diceOutcome))

            val gameResult = players map { case (name, playerState) =>
              val resultWithoutBets = GameResult(
                diceOutcome = diceOutcome,
                winningBetTypes = winningBetTypes,
                totalBet = BigDecimal(0),
                totalWin = BigDecimal(0),
                balance  = playerState.balance,
                username = name
              )
              playersBets.get(name) match {
                case None => name -> resultWithoutBets // player did not made bets
                case Some(bets) =>
                  val totalBetAmount = bets.map(_.amount.getOrElse(BigDecimal(0))).sum
                  val totalWinAmount = paytable.getWinningTotal(bets)
                  name -> resultWithoutBets.copy(
                    totalBet = totalBetAmount,
                    totalWin = totalWinAmount,
                    balance = Balance(playerState.balance.amount + totalWinAmount)
                  )
              }
            }
            val newPlayerState = updatePlayerState(gameResult)
            Right(this.copy(players = newPlayerState, gameResult = gameResult))
        }
      case _ => Left("Invalid game state")
    }
  }

  def updatePlayerState(gameResult: Map[Name, GameResult]): Map[Name, PlayerState] = {
    gameResult map { case (name, result) =>
      players.get(name) match {
        case Some(playerState) => name -> playerState.copy(balance = result.balance)
        case None => name -> PlayerState(name = name, balance = Balance(0), quit = false)
      }
    }
  }

  def setGamePhase(newPhase: GamePhase): GameState =
    this.copy(phase = newPhase)

  def numberOfBetsPlaced(player: Name): Int = {
    playersBets.get(player) match {
      case None => 0
      case Some(bets) => bets.length
    }
  }

  def getPlayerBalance(name: Name): BigDecimal = {
    players.get(name) match {
      case Some(playerState) => playerState.balance.amount
      case None => 0
    }
  }

}
