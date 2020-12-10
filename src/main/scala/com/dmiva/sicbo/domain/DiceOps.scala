package com.dmiva.sicbo.domain

/**
 * Holds the resulting value of three dice after rolling
 * @param a Value of first dice
 * @param b Value of second dice
 * @param c Value of third dice
 */
final case class DiceOutcome(a: Int, b: Int, c: Int)

final case class DiceOps(dice: DiceOutcome) {
  private val isInRange = dice.a > 0 && dice.b > 0 && dice.c > 0 &&
                          dice.a < 7 && dice.b < 7 && dice.c < 7
  private val set = Set(dice.a, dice.b, dice.c)
  private val list = List(dice.a, dice.b, dice.c)
  private val map = List(dice.a, dice.b, dice.c).groupBy(identity)
  // Although it's possible to rewrite two lines below using boolean algebra, I'll leave them in functional(!) style
  private val tripleValue = map.filter { case (_, list) => list.length == 3 }.keys.sum
  private val doubleValue = map.filter { case (_, list) => list.length >= 2 }.keys.sum
  private val isTriple: Boolean = set.size == 1 && isInRange
  private val isDouble: Boolean = set.size <= 2 && isInRange
  private val isTotal: Boolean = 4 <= list.sum && list.sum <= 17 && isInRange
  private val total: Int = list.sum

  def hasNumberCount(num: Int): Int = list.count(_ == num)
  def hasNumber(num: Int): Boolean = set.contains(num) && isInRange
  def hasCombo(a: Int, b: Int): Boolean = set.contains(a) && set.contains(b) && a != b && a > 0 && b > 0
  def hasDouble(num: Int): Boolean = isDouble && doubleValue == num && num > 0
  def hasTriple(num: Int): Boolean = isTriple && tripleValue == num && num > 0
  def hasAnyTriple: Boolean = isTriple
  def hasTotal(num: Int): Boolean = isTotal && total == num
  def isSmall: Boolean = !isTriple && isTotal && total <= 10
  def isBig: Boolean = !isTriple && isTotal && total >= 11
  def isEven: Boolean = !isTriple && isTotal && (total % 2 == 0)
  def isOdd: Boolean = !isTriple && isTotal && (total % 2 == 1)
}
