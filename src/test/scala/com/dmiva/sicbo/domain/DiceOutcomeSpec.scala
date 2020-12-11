package com.dmiva.sicbo.domain

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class DiceOutcomeSpec extends AnyFunSuite with Matchers {

  test("Dice outcomes should be correct when dice produces (1,1,1)") {
    val dice = DiceOutcome(1,1,1)

    dice.isSmall shouldEqual false
    dice.isBig shouldEqual false
    dice.isEven shouldEqual false
    dice.isOdd shouldEqual false
    dice.hasNumber(1) shouldEqual true
    dice.hasNumber(2) shouldEqual false
    dice.hasTotal(3) shouldEqual false
    dice.hasTotal(4) shouldEqual false
    dice.hasDouble(1) shouldEqual true
    dice.hasDouble(2) shouldEqual false
    dice.hasTriple(1) shouldEqual true
    dice.hasTriple(2) shouldEqual false
    dice.hasAnyTriple shouldEqual true
  }

  test("Dice outcomes should be correct when dice produces (1,2,2)") {
    val dice = DiceOutcome(1,2,2)

    dice.isSmall shouldEqual true
    dice.isBig shouldEqual false
    dice.isEven shouldEqual false
    dice.isOdd shouldEqual true
    dice.hasNumber(1) shouldEqual true
    dice.hasNumber(2) shouldEqual true
    dice.hasTotal(4) shouldEqual false
    dice.hasTotal(5) shouldEqual true
    dice.hasDouble(1) shouldEqual false
    dice.hasDouble(2) shouldEqual true
    dice.hasTriple(1) shouldEqual false
    dice.hasTriple(2) shouldEqual false
    dice.hasAnyTriple shouldEqual false
  }

  test("Dice outcomes should be correct when dice produces (3,4,5)") {
    val dice = DiceOutcome(3,4,5)

    dice.isSmall shouldEqual false
    dice.isBig shouldEqual true
    dice.isEven shouldEqual true
    dice.isOdd shouldEqual false
    dice.hasNumber(6) shouldEqual false
    dice.hasNumber(4) shouldEqual true
    dice.hasTotal(17) shouldEqual false
    dice.hasTotal(12) shouldEqual true
    dice.hasDouble(4) shouldEqual false
    dice.hasDouble(5) shouldEqual false
    dice.hasTriple(4) shouldEqual false
    dice.hasTriple(6) shouldEqual false
    dice.hasAnyTriple shouldEqual false
  }

  test("Dice outcomes should be correct when dice produces (1,3,6)") {
    val dice = DiceOutcome(1,3,6)

    dice.isSmall shouldEqual true
    dice.isBig shouldEqual false
    dice.isEven shouldEqual true
    dice.isOdd shouldEqual false
    dice.hasNumber(1) shouldEqual true
    dice.hasNumber(2) shouldEqual false
    dice.hasNumber(3) shouldEqual true
    dice.hasNumber(4) shouldEqual false
    dice.hasNumber(5) shouldEqual false
    dice.hasNumber(6) shouldEqual true
    dice.hasTotal(13) shouldEqual false
    dice.hasTotal(10) shouldEqual true
    dice.hasDouble(4) shouldEqual false
    dice.hasDouble(5) shouldEqual false
    dice.hasTriple(4) shouldEqual false
    dice.hasTriple(6) shouldEqual false
    dice.hasAnyTriple shouldEqual false
  }

}
