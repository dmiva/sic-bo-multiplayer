package com.dmiva.sicbo.domain

final case class DiceOutcome(a: Int, b: Int, c: Int)

final case class DiceOps(dice: DiceOutcome) {
  private val set = Set(dice.a, dice.b, dice.c)
  private val list = List(dice.a, dice.b, dice.c)
  private val map = List(dice.a, dice.b, dice.c).groupBy(identity)
  private val tripleValue = map.filter { case (_, list) => list.length == 3 }.keys.sum
  private val doubleValue = map.filter { case (_, list) => list.length >= 2 }.keys.sum
  private val isTriple: Boolean = set.size == 1
  private val isDouble: Boolean = set.size == 2
  private val isTotal: Boolean = 4 <= set.sum && set.sum <= 17
  private val total: Int = set.sum

  def hasNumberCount(num: Int): Int = list.count(_ == num)
  def hasNumber(num: Int): Boolean = set.contains(num)
  def hasCombo(a: Int, b: Int): Boolean = set.contains(a) && set.contains(b)
  def hasDouble(num: Int): Boolean = isDouble && doubleValue == num
  def hasTriple(num: Int): Boolean = isTriple && tripleValue == num
  def hasAnyTriple: Boolean = isTriple
  def hasTotal(num: Int): Boolean = isTotal && total == num
  def isSmall: Boolean = !isTriple && total <= 10
  def isBig: Boolean = !isTriple && total >= 11
  def isEven: Boolean = !isTriple && total % 2 == 0
  def isOdd: Boolean = !isTriple && total % 2 == 1
}
