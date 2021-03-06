package com.dmiva.sicbo.domain

/**
 * Holds the resulting value of three dice after rolling
 * @param a Value of first dice
 * @param b Value of second dice
 * @param c Value of third dice
 */
final case class DiceOutcome private (a: Int, b: Int, c: Int) {
  private val isInRange = a > 0 && b > 0 && c > 0 &&
                          a < 7 && b < 7 && c < 7
  private val set = Set(a, b, c)
  private val list = List(a, b, c)
  private val map = List(a, b, c).groupBy(identity)
  private val tripleValue = map.filter { case (_, list) => list.length == 3 }.keys.sum
  private val doubleValue = map.filter { case (_, list) => list.length >= 2 }.keys.sum
  private val isTriple: Boolean = set.size == 1 && isInRange
  private val isDouble: Boolean = set.size <= 2 && isInRange
  private val isTotal: Boolean = 4 <= list.sum && list.sum <= 17 && isInRange
  private val total: Int = list.sum

  def getTotal: Int = total
  def numberCount(num: Int): Int = list.count(_ == num)
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

object DiceOutcome {
  def from(a: Int, b: Int, c: Int): Option[DiceOutcome] =
    if (a >= 1 && a <= 6 && b >= 1 && b <= 6 && c >= 1 && c <= 6) Some(DiceOutcome(a, b, c)) else None

  def from(a: String, b: String, c: String): Option[DiceOutcome] =
    (a.toIntOption, b.toIntOption, c.toIntOption) match {
      case (Some(a), Some(b), Some(c))  => Some(DiceOutcome(a, b, c))
      case _                            => None
    }

}
