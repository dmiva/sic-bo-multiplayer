package com.dmiva.sicbo.domain

import BetType._
/**
 * Payouts for a specific combination of dice.
 * Applies base payout multipliers to the name's bets according to game specification.
 * Does not filters out losing bets from supplied input data.
 * @param dice Combination of three dice
 */
final case class Paytable(dice: DiceOutcome) {

  private def totalPayout(num: Int): Int = num match {
    case 4 | 17 => 50
    case 5 | 16 => 20
    case 6 | 15 => 15
    case 7 | 14 => 12
    case 8 | 13 => 8
    case 9 | 12 => 6
    case 10 | 11 => 6
  }

  private def numberPayout(num: Int): Int = dice.numberCount(num) match {
    case 1 => 1
    case 2 => 2
    case 3 => 3
  }

  private val triplePayout: Int = 150
  private val anyTriplePayout: Int = 30
  private val doublePayout: Int = 8
  private val comboPayout: Int = 5
  private val payoutIfTripple: Int = if (dice.hasAnyTriple) 0 else 1
  private val bigPayout: Int = payoutIfTripple
  private val smallPayouyt: Int = payoutIfTripple
  private val evenPayout: Int = payoutIfTripple
  private val oddPayout: Int = payoutIfTripple

  private def getNumberPayout(num: Int): Int = if (dice.hasNumber(num)) numberPayout(num) else 0
  private def getComboPayout(a: Int, b: Int): Int = if (dice.hasCombo(a, b)) comboPayout else 0
  private def getTotalPayout(num: Int): Int = if (dice.hasTotal(num)) totalPayout(num) else 0
  private def getDoublePayout(num: Int): Int = if (dice.hasDouble(num)) doublePayout else 0
  private def getTriplePayout(num: Int): Int = if (dice.hasTriple(num)) triplePayout else 0
  private def getAnyTriplePayout: Int = if (dice.hasAnyTriple) anyTriplePayout else 0
  private def getBigPayout: Int = if (dice.isBig) bigPayout else 0
  private def getSmallPayout: Int = if (dice.isSmall) smallPayouyt else 0
  private def getEvenPayout: Int = if (dice.isEven) evenPayout else 0
  private def getOddPayout: Int = if (dice.isOdd) oddPayout else 0

  def applyPayout(bet: Bet): Bet = {
    val betAmount: BigDecimal = bet.amount.getOrElse(BigDecimal(0))
    val multiplier: Int = bet.betType match {
      case Small        => getSmallPayout
      case Big          => getBigPayout
      case Even         => getEvenPayout
      case Odd          => getOddPayout
      case Number(num)  => getNumberPayout(num)
      case Total(num)   => getTotalPayout(num)
      case Combo(a, b)  => getComboPayout(a, b)
      case Double(num)  => getDoublePayout(num)
      case Triple(num)  => getTriplePayout(num)
      case AnyTriple    => getAnyTriplePayout
    }
    bet.copy(win = {
      if (multiplier > 0) Some((multiplier * betAmount) + betAmount)
      else Some(0)
    })
  }

  def applyPayout(bets: List[Bet]): List[Bet] = {
    bets.map(bet => applyPayout(bet))
  }

  def getWinning(bet: Bet): BigDecimal = {
    applyPayout(bet).win match {
      case Some(win) => win
      case None => 0
    }
  }

  def getWinningTotal(bets: List[Bet]): BigDecimal = applyPayout(bets).map(bet => getWinning(bet)).sum
}

