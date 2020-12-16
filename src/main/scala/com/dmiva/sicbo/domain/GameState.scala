package com.dmiva.sicbo.domain

import com.dmiva.sicbo.common.OutgoingMessage.GameResult
import com.dmiva.sicbo.domain.Player.{Balance, Player, PlayerInfo}

object GameState {

  def startNewGame: GameState = GameState(
    phase = GamePhase.PlacingBets,
    playersBets = Map.empty,
    dice = None,
    gameResult = Map.empty
  )
}

final case class GameState(
                            phase: GamePhase,
                            playersBets: Map[Player, List[Bet]],
                            dice: Option[DiceOutcome],
                            gameResult: Map[Player, GameResult]
                          ) {

  def placeBet(player: Player, bets: List[Bet]): Option[GameState] = {
    phase match {
      case GamePhase.PlacingBets =>
        Some(this.copy(playersBets = playersBets + (player -> playersBets.getOrElse(player,List.empty).concat(bets))))
      case _ => None
    }
  }

  def applyDiceOutcome(dice: DiceOutcome): Option[GameState] = {
    phase match {
      case GamePhase.RollingDice =>
        Some(this.copy(dice = Some(dice)))
      case _ => None
    }
  }

  def calculateResults: Option[GameState] = {
    phase match {
      case GamePhase.MakePayouts =>
        dice match {
          case None => None
          case Some(diceOutcome) =>
            val paytable = Paytable(diceOutcome)
            val winningBetTypes = BetType.allBets.filter(b => Bet.isWinningBetType(b, diceOutcome))
            val gameResult = playersBets map {
              case (player, bets) =>
                player -> GameResult(
                  diceOutcome = diceOutcome,
                  winningBetTypes = winningBetTypes,
                  totalWin = paytable.getWinningTotal(bets),
                  playerInfo = PlayerInfo.from(
                    player.copy(balance = player.balance + Balance(paytable.getWinningTotal(bets)))
                  )
                )
            }
            Some(this.copy(gameResult = gameResult))
        }
      case _ => None
    }
  }

  def setGamePhase(newPhase: GamePhase): GameState =
    this.copy(phase = newPhase)

  def numberOfBetsPlaced(player: Player): Int = {
    playersBets.get(player) match {
      case None => 0
      case Some(bets) => bets.length
    }
  }

}
