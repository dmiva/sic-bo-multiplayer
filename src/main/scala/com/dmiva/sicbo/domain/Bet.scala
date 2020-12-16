package com.dmiva.sicbo.domain

import com.dmiva.sicbo.domain.BetType._

final case class Bet(amount: Option[Int], betType: BetType, win: Option[Int])

object Bet {

  def isWinningBetType(betType: BetType, dice: DiceOutcome): Boolean = {

    betType match {
      case Small       if dice.isSmall         => true
      case Big         if dice.isBig           => true
      case Even        if dice.isEven          => true
      case Odd         if dice.isOdd           => true
      case Number(num) if dice.hasNumber(num)  => true
      case Total(num)  if dice.hasTotal(num)   => true
      case Combo(a, b) if dice.hasCombo(a, b)  => true
      case Double(num) if dice.hasDouble(num)  => true
      case Triple(num) if dice.hasTriple(num)  => true
      case AnyTriple   if dice.hasAnyTriple    => true
      case _                                   => false
    }
  }

  def isWinningBet(bet: Bet, diceOutcome: DiceOutcome): Boolean = {
    isWinningBetType(bet.betType, diceOutcome)
  }

  def winningBets(bets: List[Bet], diceOutcome: DiceOutcome): List[Bet] = {
    bets.filter(bet => isWinningBet(bet, diceOutcome))
  }

}





