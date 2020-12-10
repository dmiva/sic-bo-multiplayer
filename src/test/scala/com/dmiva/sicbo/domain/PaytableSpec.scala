package com.dmiva.sicbo.domain

import org.scalacheck.Gen.choose
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class PaytableSpec extends AnyFunSuite with Matchers with ScalaCheckDrivenPropertyChecks {



  test("test1 ") {
    val dice = DiceOutcome(1,2,3)
    val pay = Paytable(dice)
    val bet = Bet(Some(10), BetType.Small, None)

    pay.applyPayout(bet) shouldEqual(bet.copy(win = Some(20)))

  }

  test("Number bets should have correct payout with randomized input") {
    forAll(
      choose(1, 6),
      choose(1, 6),
      choose(1, 6),
      choose(1, 6),
      choose(1, 6)
    ) { case (amount, number, a, b, c) =>

      val nonZero = a > 0 && b > 0 && c > 0 && amount > 0
      val result = ((amount == a) || (amount == b) || (amount == c)) && nonZero

      val dice = DiceOutcome(1,2,3)
      val paytable = Paytable(dice)
      val bet = Bet(Some(amount), BetType.Number(number), None)

//      val winAmount =

       paytable.getWinning(bet) shouldEqual (amount * 2)

    }
  }

}
