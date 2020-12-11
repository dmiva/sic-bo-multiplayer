package com.dmiva.sicbo.domain

import org.scalacheck.Gen.choose
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class PaytableSpec extends AnyFunSuite with Matchers with ScalaCheckDrivenPropertyChecks {

  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 200)


  // Constant payouts that are independent from dice combination
  def totalPayout(num: Int): Int = num match {
    case 4 | 17 => 50
    case 5 | 16 => 20
    case 6 | 15 => 15
    case 7 | 14 => 12
    case 8 | 13 => 8
    case 9 | 12 => 6
    case 10 | 11 => 6
  }

  val triplePayout: Int = 150
  val anyTriplePayout: Int = 30
  val doublePayout: Int = 8
  val comboPayout: Int = 5

  test("Number bets should have correct payout with randomized input") {
    forAll(
      choose(1, 1000),
      choose(1, 6),
      choose(1, 6),
      choose(1, 6),
      choose(1, 6)
    ) { case (amount, num, a, b, c) =>

      val dice = DiceOutcome(a,b,c)
      val paytable = Paytable(dice)
      val bet = Bet(Some(amount), BetType.Number(num), None)

      val winAmount = paytable.getWinning(bet)
      val expectedWinAmount = if (dice.hasNumber(num))
        (dice.numberCount(num) * amount) + amount else 0

      winAmount shouldEqual expectedWinAmount
    }
  }

  test("Combo bets should have correct payout with randomized input") {
    forAll(
      choose(1, 1000),
      choose(1, 6),
      choose(1, 6),
      choose(1, 6),
      choose(1, 6),
      choose(1, 6)
    ) { case (amount, num1, num2, a, b, c) =>

      val dice = DiceOutcome(a,b,c)
      val paytable = Paytable(dice)
      val bet = Bet(Some(amount), BetType.Combo(num1, num2), None)

      val winAmount = paytable.getWinning(bet)
      val expectedWinAmount = if (dice.hasCombo(num1, num2))
        (comboPayout * amount) + amount else 0

      winAmount shouldEqual expectedWinAmount
    }
  }

  test("Total bets should have correct payout with randomized input") {
    forAll(
      choose(1, 1000),
      choose(4, 17),
      choose(1, 6),
      choose(1, 6),
      choose(1, 6)
    ) { case (amount, num, a, b, c) =>

      val dice = DiceOutcome(a,b,c)
      val paytable = Paytable(dice)
      val bet = Bet(Some(amount), BetType.Total(num), None)

      val winAmount = paytable.getWinning(bet)
      val expectedWinAmount = if (dice.hasTotal(num))
        (totalPayout(num) * amount) + amount else 0

      winAmount shouldEqual expectedWinAmount
    }
  }

  test("Double bets should have correct payout with randomized input") {
    forAll(
      choose(1, 1000),
      choose(1, 6),
      choose(1, 6),
      choose(1, 6),
      choose(1, 6)
    ) { case (amount, num, a, b, c) =>

      val dice = DiceOutcome(a,b,c)
      val paytable = Paytable(dice)
      val bet = Bet(Some(amount), BetType.Double(num), None)

      val winAmount = paytable.getWinning(bet)
      val expectedWinAmount = if (dice.hasDouble(num))
        (doublePayout * amount) + amount else 0

      winAmount shouldEqual expectedWinAmount
    }
  }

  test("Triple bets should have correct payout with randomized input") {
    forAll(
      choose(1, 1000),
      choose(1, 6),
      choose(1, 6),
      choose(1, 6),
      choose(1, 6)
    ) { case (amount, num, a, b, c) =>

      val dice = DiceOutcome(a,b,c)
      val paytable = Paytable(dice)
      val bet = Bet(Some(amount), BetType.Triple(num), None)

      val winAmount = paytable.getWinning(bet)
      val expectedWinAmount = if (dice.hasTriple(num))
        (triplePayout * amount) + amount else 0

      winAmount shouldEqual expectedWinAmount
    }
  }

  test("AnyTriple bets should have correct payout with randomized input") {
    forAll(
      choose(1, 1000),
      choose(1, 6),
      choose(1, 6),
      choose(1, 6)
    ) { case (amount, a, b, c) =>

      val dice = DiceOutcome(a,b,c)
      val paytable = Paytable(dice)
      val bet = Bet(Some(amount), BetType.AnyTriple, None)

      val winAmount = paytable.getWinning(bet)
      val expectedWinAmount = if (dice.hasAnyTriple)
        (anyTriplePayout * amount) + amount else 0

      winAmount shouldEqual expectedWinAmount
    }
  }

  test("Big bets should have correct payout with randomized input") {
    forAll(
      choose(1, 1000),
      choose(1, 6),
      choose(1, 6),
      choose(1, 6)
    ) { case (amount, a, b, c) =>

      val dice = DiceOutcome(a,b,c)
      val paytable = Paytable(dice)
      val bet = Bet(Some(amount), BetType.Big, None)

      val winAmount = paytable.getWinning(bet)
      val expectedWinAmount = if (dice.isBig && !dice.hasAnyTriple)
        (1 * amount) + amount else 0

      winAmount shouldEqual expectedWinAmount
    }
  }

  test("Small bets should have correct payout with randomized input") {
    forAll(
      choose(1, 1000),
      choose(1, 6),
      choose(1, 6),
      choose(1, 6)
    ) { case (amount, a, b, c) =>

      val dice = DiceOutcome(a,b,c)
      val paytable = Paytable(dice)
      val bet = Bet(Some(amount), BetType.Small, None)

      val winAmount = paytable.getWinning(bet)
      val expectedWinAmount = if (dice.isSmall && !dice.hasAnyTriple)
        (1 * amount) + amount else 0

      //      println(s"winAmount = $winAmount, expected = $expectedWinAmount")
      winAmount shouldEqual expectedWinAmount
    }
  }

  test("Even bets should have correct payout with randomized input") {
    forAll(
      choose(1, 1000),
      choose(1, 6),
      choose(1, 6),
      choose(1, 6)
    ) { case (amount, a, b, c) =>

      val dice = DiceOutcome(a,b,c)
      val paytable = Paytable(dice)
      val bet = Bet(Some(amount), BetType.Even, None)

      val winAmount = paytable.getWinning(bet)
      val expectedWinAmount = if (dice.isEven && !dice.hasAnyTriple)
        (1 * amount) + amount else 0

      winAmount shouldEqual expectedWinAmount
    }
  }

  test("Odd bets should have correct payout with randomized input") {
    forAll(
      choose(1, 1000),
      choose(1, 6),
      choose(1, 6),
      choose(1, 6)
    ) { case (amount, a, b, c) =>

      val dice = DiceOutcome(a,b,c)
      val paytable = Paytable(dice)
      val bet = Bet(Some(amount), BetType.Odd, None)

      val winAmount = paytable.getWinning(bet)
      val expectedWinAmount = if (dice.isOdd && !dice.hasAnyTriple)
        (1 * amount) + amount else 0

      winAmount shouldEqual expectedWinAmount
    }
  }

  test("Bets should have correct payout with manually written input #1") {
    val dice = DiceOutcome(4,1,4)
    val paytable = Paytable(dice)

    val b1 = Bet(Some(10), BetType.Small, None) // 20
    val b2 = Bet(Some(10), BetType.Big, None) // 0
    val b3 = Bet(Some(10), BetType.Even, None) // 0
    val b4 = Bet(Some(10), BetType.Odd, None) // 20
    val b5 = Bet(Some(10), BetType.Number(4), None) // 30
    val b6 = Bet(Some(10), BetType.Total(9), None) // 70
    val b7 = Bet(Some(10), BetType.Combo(1,4), None) // 60
    val b8 = Bet(Some(10), BetType.Double(6), None) // 0
    val b9 = Bet(Some(10), BetType.Triple(2), None) // 0
    val b10 = Bet(Some(10), BetType.AnyTriple, None) // 0

    val listOfBets = List(b1,b2,b3,b4,b5,b6,b7,b8,b9,b10)

    val winAmount = paytable.getWinning(listOfBets)
    val expectedWinAmount = 20 + 20 + 30 + 60 + 70

    winAmount shouldEqual expectedWinAmount
  }

  test("Bets should have correct payout with manually written input #2") {
    val dice = DiceOutcome(3,3,3)
    val paytable = Paytable(dice)

    val b1 = Bet(Some(10), BetType.Small, None) // 0  - because of triple
    val b2 = Bet(Some(10), BetType.Big, None) // 0
    val b3 = Bet(Some(10), BetType.Even, None) // 0
    val b4 = Bet(Some(10), BetType.Odd, None) // 0 - because of triple
    val b5 = Bet(Some(10), BetType.Number(3), None) // 40
    val b6 = Bet(Some(10), BetType.Total(9), None) // 70
    val b7 = Bet(Some(10), BetType.Combo(3,4), None) // 0
    val b8 = Bet(Some(10), BetType.Double(3), None) // 90
    val b9 = Bet(Some(10), BetType.Triple(3), None) // 1510
    val b10 = Bet(Some(10), BetType.AnyTriple, None) // 310

    val listOfBets = List(b1,b2,b3,b4,b5,b6,b7,b8,b9,b10)

    val winAmount = paytable.getWinning(listOfBets)
    val expectedWinAmount = 40 + 70 + 90 + 1510 + 310

    winAmount shouldEqual expectedWinAmount
  }

  test("Bets should have correct payout with manually written input #3") {
    val dice = DiceOutcome(4,5,6)
    val paytable = Paytable(dice)

    val b1 = Bet(Some(10), BetType.Small, None) // 0
    val b2 = Bet(Some(10), BetType.Big, None) // 20
    val b3 = Bet(Some(10), BetType.Even, None) // 0
    val b4 = Bet(Some(10), BetType.Odd, None) // 20
    val b5 = Bet(Some(10), BetType.Number(4), None) // 20
    val b6 = Bet(Some(10), BetType.Total(15), None) // 160
    val b7 = Bet(Some(10), BetType.Combo(4,6), None) // 60
    val b8 = Bet(Some(10), BetType.Double(5), None) // 0
    val b9 = Bet(Some(10), BetType.Triple(2), None) // 0
    val b10 = Bet(Some(10), BetType.AnyTriple, None) // 0

    val listOfBets = List(b1,b2,b3,b4,b5,b6,b7,b8,b9,b10)

    val winAmount = paytable.getWinning(listOfBets)
    val expectedWinAmount = 20 + 20 + 20 + 160 + 60

    winAmount shouldEqual expectedWinAmount
  }

  test("Cross check - determination of winning bet and non zero payout should be in sync") {
    forAll(
      choose(4, 17),
      choose(1, 6),
      choose(1, 6),
      choose(1, 6),
      choose(1, 6),
      choose(1, 6)
    ) { case (total, num1, num2, a, b, c) =>

      val dice = DiceOutcome(a,b,c)
      val paytable = Paytable(dice)

      val bet1 = Bet(Some(10), BetType.Number(num1), None)
      val bet2 = Bet(Some(10), BetType.Total(total), None)
      val bet3 = Bet(Some(10), BetType.Combo(num1, num2), None)
      val bet4 = Bet(Some(10), BetType.Double(num1), None)
      val bet5 = Bet(Some(10), BetType.Triple(num1), None)
      val bet6 = Bet(Some(10), BetType.Big, None)
      val bet7 = Bet(Some(10), BetType.Small, None)
      val bet8 = Bet(Some(10), BetType.Odd, None)
      val bet9 = Bet(Some(10), BetType.Even, None)
      val bet10 = Bet(Some(10), BetType.AnyTriple, None)

      val (won1a, won1b) = (paytable.getWinning(bet1) > 0, Bet.isWinningBet(bet1, dice))
      val (won2a, won2b) = (paytable.getWinning(bet2) > 0, Bet.isWinningBet(bet2, dice))
      val (won3a, won3b) = (paytable.getWinning(bet3) > 0, Bet.isWinningBet(bet3, dice))
      val (won4a, won4b) = (paytable.getWinning(bet4) > 0, Bet.isWinningBet(bet4, dice))
      val (won5a, won5b) = (paytable.getWinning(bet5) > 0, Bet.isWinningBet(bet5, dice))
      val (won6a, won6b) = (paytable.getWinning(bet6) > 0, Bet.isWinningBet(bet6, dice))
      val (won7a, won7b) = (paytable.getWinning(bet7) > 0, Bet.isWinningBet(bet7, dice))
      val (won8a, won8b) = (paytable.getWinning(bet8) > 0, Bet.isWinningBet(bet8, dice))
      val (won9a, won9b) = (paytable.getWinning(bet9) > 0, Bet.isWinningBet(bet9, dice))
      val (won10a, won10b) = (paytable.getWinning(bet10) > 0, Bet.isWinningBet(bet10, dice))

      won1a shouldEqual won1b
      won2a shouldEqual won2b
      won3a shouldEqual won3b
      won4a shouldEqual won4b
      won5a shouldEqual won5b
      won6a shouldEqual won6b
      won7a shouldEqual won7b
      won8a shouldEqual won8b
      won9a shouldEqual won9b
      won10a shouldEqual won10b
    }
  }

}
