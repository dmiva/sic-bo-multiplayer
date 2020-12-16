package com.dmiva.sicbo.domain

import com.dmiva.sicbo.common.BetRejectReason
import com.dmiva.sicbo.common.OutgoingMessage.GameResult
import com.dmiva.sicbo.domain.Player.{Balance, Name, Player, PlayerInfo}

object GameState {



  def initGame: GameState = GameState(
    phase = GamePhase.Idle,
    players = List.empty,
    playersBets = Map.empty,
    dice = None,
    gameResult = Map.empty
  )
}

final case class GameState(
                            phase: GamePhase,
                            players: List[PlayerInfo],
                            playersBets: Map[Player, List[Bet]], // Player key does not change during game period
                            dice: Option[DiceOutcome],
                            gameResult: Map[Player, GameResult]
                          ) {

  /**
   * Starts new game session. All bets and payout are cleaned.
   * Player data is retained.
   */
  def startNewGame: GameState = this.copy(
    phase = GamePhase.PlacingBets,
    playersBets = Map.empty,
    dice = None,
    gameResult = Map.empty
  )

  def placeBet(player: Player, bets: List[Bet]): Either[String, GameState] = {
    phase match {
      case GamePhase.PlacingBets => // TODO: Check balance
        val totalPlacedBetAmount = playersBets.getOrElse(player, List.empty).map(_.amount.getOrElse(0)).sum
        val newBetAmount = bets.map(_.amount.getOrElse(0)).sum
//        val playerBalance = getPlayerBalance(player.username)
        if (player.balance.amount >= (totalPlacedBetAmount + newBetAmount)) // TODO: Bug - player is immutable reference
          Right(this.copy(playersBets = playersBets + (player -> playersBets.getOrElse(player,List.empty).concat(bets))))
        else Left(BetRejectReason.NotEnoughBalance)
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

  def calculateResults: Either[String, GameState] = {
    phase match {
      case GamePhase.MakePayouts =>
        dice match {
          case None => Left("Dice value is not known")
          case Some(diceOutcome) =>
            val paytable = Paytable(diceOutcome)
            val winningBetTypes = BetType.allBets.filter(b => Bet.isWinningBetType(b, diceOutcome))
            val gameResult = playersBets map {
              case (player, bets) =>
                val totalPlacedBetAmount = playersBets.getOrElse(player, List.empty).map(_.amount.getOrElse(0)).sum
                val totalWinAmount = paytable.getWinningTotal(bets)
                val balanceDelta = totalWinAmount - totalPlacedBetAmount
                player -> GameResult(
                  diceOutcome     = diceOutcome,
                  winningBetTypes = winningBetTypes,
                  totalWin        = totalWinAmount,
                  playerInfo      = PlayerInfo.from(
                    player.copy(
                      balance = player.balance + Balance(balanceDelta))
                  )
                )
            }
            Right(this.copy(gameResult = gameResult))
        }
      case _ => Left("Invalid game state")
    }
  }

  def getGameResultFor(player: Player): Option[GameResult] = {
    gameResult.get(player)
  }

  def getGameResultForName(name: Name): Option[(Player, GameResult)] = {
    gameResult.find { case (player, _) => player.username == name }
  }

  def setGamePhase(newPhase: GamePhase): GameState =
    this.copy(phase = newPhase)

  def numberOfBetsPlaced(player: Player): Int = {
    playersBets.get(player) match {
      case None => 0
      case Some(bets) => bets.length
    }
  }

  def getPlayerBalance(name: Name): Either[String, Balance] = {
    players.find(_.username == name) match {
      case Some(value) => Right(value.balance)
      case None => Left("Player not found")
    }
  }

}
