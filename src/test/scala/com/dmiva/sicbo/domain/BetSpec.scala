package com.dmiva.sicbo.domain

import org.scalacheck.Gen.choose
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class BetSpec extends AnyFunSuite with Matchers with ScalaCheckDrivenPropertyChecks {

  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 200)

  test("Number bets should be evaluated correctly with randomized input") {
    forAll(
      choose(1, 6),
      choose(1, 6),
      choose(1, 6),
      choose(1, 6)
    ) { case (bet, a, b, c) =>
        val nonZero = a > 0 && b > 0 && c > 0 && bet > 0
        val result = ((bet == a) || (bet == b) || (bet == c)) && nonZero
        Bet.isWinningBetType(BetType.Number(0), DiceOutcome(a, b, c)) shouldEqual false
        Bet.isWinningBetType(BetType.Number(a), DiceOutcome(a, b, c)) shouldEqual true
        Bet.isWinningBetType(BetType.Number(b), DiceOutcome(a, b, c)) shouldEqual true
        Bet.isWinningBetType(BetType.Number(c), DiceOutcome(a, b, c)) shouldEqual true
        Bet.isWinningBetType(BetType.Number(bet), DiceOutcome(a, b, c)) shouldEqual result
      }
  }

  test("Combo bets should be evaluated correctly with randomized input") {
    forAll(
      choose(1, 6),
      choose(1, 6),
      choose(1, 6),
      choose(1, 6),
      choose(1, 6)
    ) { case (num1, num2, a, b, c) =>

        val result =  (num1 == a || num1 == b || num1 == c) &&
                      (num2 == a || num2 == b || num2 == c) &&
                      (num1 != num2)

        Bet.isWinningBetType(BetType.Combo(0, 0), DiceOutcome(a, b, c)) shouldEqual false
        Bet.isWinningBetType(BetType.Combo(num1, num2), DiceOutcome(a, b, c)) shouldEqual result
      }
  }

  test("Total bets should be evaluated correctly with randomized input") {
    forAll(
      choose(1, 18),
      choose(1, 6),
      choose(1, 6),
      choose(1, 6)
    ) { case (bet, a, b, c) =>

      val sum = a + b + c
      val sumInRange = 4 <= sum && sum <= 17
      val result2 = (bet == sum) && sumInRange

      DiceOutcome(a,b,c).hasTotal(sum) shouldEqual sumInRange
      Bet.isWinningBetType(BetType.Total(a + b + c), DiceOutcome(a, b, c)) shouldEqual sumInRange
      Bet.isWinningBetType(BetType.Total(bet), DiceOutcome(a, b, c)) shouldEqual result2
    }
  }

  test("Double bets should be evaluated correctly with randomized input") {
    forAll(
      choose(1, 6), // this is bet to Double (from 1 to 6)
      choose(1, 6), // Dice
      choose(1, 6), // Dice
      choose(1, 6) // Dice
    ) { case (bet, a, b, c) =>

        val nonZero = a > 0 && b > 0 && c > 0
        val result: Boolean = (a == b || a == c || b == c) && nonZero
        val doubleValue = if (a == b) a
        else if (a == c) a
        else if (b == c) b else 0
        println(s"Bet: Double($bet) , Dice: $a $b $c , gameResult = ${if (doubleValue == bet) "won" else "lost"}")

        DiceOutcome(a,b,c).hasDouble(doubleValue) shouldEqual result
        Bet.isWinningBetType(BetType.Double(0), DiceOutcome(a,b,c)) shouldEqual false
        Bet.isWinningBetType(BetType.Double(bet), DiceOutcome(a,b,c)) shouldEqual (doubleValue == bet)
      }
    DiceOutcome(5,5,5).hasDouble(5) shouldEqual true
  }

  test("Triple bets should be evaluated correctly with randomized input") {
    forAll(
      choose(1, 6),
      choose(1, 6),
      choose(1, 6),
      choose(1, 6)
    ) { case (bet, a, b, c) =>

        val result: Boolean = a == b && a == c
        val tripleValue = if (result) a else 0

        DiceOutcome(a,b,c).hasAnyTriple shouldEqual result
        DiceOutcome(a,b,c).hasTriple(tripleValue) shouldEqual result
        Bet.isWinningBetType(BetType.Triple(bet), DiceOutcome(a,b,c)) shouldEqual (tripleValue == bet)
      }
  }

  test("Big bet should be evaluated correctly with randomized input") {
    forAll(
      choose(1, 6),
      choose(1, 6),
      choose(1, 6)
    ) { case (a, b, c) =>

      val sum = a + b + c
      val isTriple = a == b && a == c

      val isBig =  isTriple && (sum == 11 || sum == 13 || sum == 14 || sum == 16 || sum == 17) ||
                  !isTriple && (sum == 11 || sum == 12 || sum == 13 || sum == 14 || sum == 15 || sum == 16 || sum == 17)


      DiceOutcome(a, b, c).isBig shouldEqual isBig
      Bet.isWinningBetType(BetType.Big, DiceOutcome(a, b, c)) shouldEqual isBig
    }
  }

  test("Small bet should be evaluated correctly with randomized input") {
    forAll(
      choose(1, 6),
      choose(1, 6),
      choose(1, 6)
    ) { case (a, b, c) =>

      val sum = a + b + c
      val isTriple = a == b && a == c

      val isSmall = isTriple && (sum == 4 || sum == 5 || sum == 7 || sum == 8 || sum == 10) ||
        !isTriple && (sum == 4 || sum == 5 || sum == 6 || sum == 7 || sum == 8 || sum == 9 || sum == 10)


      DiceOutcome(a, b, c).isSmall shouldEqual isSmall
      Bet.isWinningBetType(BetType.Small, DiceOutcome(a, b, c)) shouldEqual isSmall
    }
  }

  test("Even bet should be evaluated correctly with randomized input") {
    forAll(
      choose(1, 6),
      choose(1, 6),
      choose(1, 6)
    ) { case (a, b, c) =>

      val sumIsEven = (a + b + c) % 2 == 0
      val isTriple = a == b && a == c
      val result = !isTriple && sumIsEven

      DiceOutcome(a, b, c).isEven shouldEqual result
      Bet.isWinningBetType(BetType.Even, DiceOutcome(a, b, c)) shouldEqual result
    }
  }

  test("Odd bet should be evaluated correctly with randomized input") {
    forAll(
      choose(1, 6),
      choose(1, 6),
      choose(1, 6)
    ) { case (a, b, c) =>

      val sumIsOdd = (a + b + c) % 2 == 1
      val isTriple = a == b && a == c
      val result = !isTriple && sumIsOdd

      DiceOutcome(a, b, c).isOdd shouldEqual result
      Bet.isWinningBetType(BetType.Odd, DiceOutcome(a, b, c)) shouldEqual result
    }
  }

  test("winningBets bet should work correctly with randomized input") {
    forAll(
      choose(5, 10)
    ) { numOfBets =>
      for (_ <- 0 to numOfBets) {
        var list: collection.mutable.Seq[Bet] = collection.mutable.Seq[Bet]()
        forAll(
          choose(0, 6),
          choose(0, 6),
          choose(0, 6),
          choose(0, 6),
          choose(0, 6)
        ) { case (num1, num2, a, b, c) =>

          val dice = DiceOutcome(a, b, c)

          val countOfWinningBets = List(
            dice.isBig,
            dice.isSmall,
            dice.isEven,
            dice.isOdd,
            dice.hasNumber(a),
            dice.hasNumber(b),
            dice.hasNumber(c),
            dice.hasNumber(num1),
            dice.hasNumber(num2),
            dice.hasTotal(a + b + c),
            dice.hasCombo(num1, num2),
            dice.hasDouble(num1),
            dice.hasDouble(num2),
            dice.hasTriple(num1),
            dice.hasTriple(num2),
            dice.hasAnyTriple
          ).count(_ == true)

          list = list :+ Bet(None, BetType.Big, None)
          list = list :+ Bet(None, BetType.Small, None)
          list = list :+ Bet(None, BetType.Even, None)
          list = list :+ Bet(None, BetType.Odd, None)
          list = list :+ Bet(None, BetType.Number(a), None)
          list = list :+ Bet(None, BetType.Number(b), None)
          list = list :+ Bet(None, BetType.Number(c), None)
          list = list :+ Bet(None, BetType.Number(num1), None)
          list = list :+ Bet(None, BetType.Number(num2), None)
          list = list :+ Bet(None, BetType.Total(a + b + c), None)
          list = list :+ Bet(None, BetType.Combo(num1, num2), None)
          list = list :+ Bet(None, BetType.Double(num1), None)
          list = list :+ Bet(None, BetType.Double(num2), None)
          list = list :+ Bet(None, BetType.Triple(num1), None)
          list = list :+ Bet(None, BetType.Triple(num2), None)
          list = list :+ Bet(None, BetType.AnyTriple, None)

          val listOfBets = list.toList
          Bet.winningBets(listOfBets, DiceOutcome(a, b, c)).length shouldEqual countOfWinningBets
          list = list.empty
        }
      }
    }
  }

}
