package com.dmiva.sicbo.domain

import com.dmiva.sicbo.domain.BetType.{OtherBet, SingleBet}


sealed trait BetType
object BetType {
  final case object SingleBet extends BetType
  final case object ComboBet extends BetType
  final case object DoubleBet extends BetType
  final case object TripleBet extends BetType
  final case object TotalBet extends BetType
  final case object OtherBet extends BetType
}

//sealed abstract class Bet private (val id: Int, val betType: BetType, val baseMultiplier: Int)
sealed abstract class Bet private (val id: Int, val baseMultiplier: Int)

//object tests extends App {
//
//  val dice = List(1,2,1)
//  val combos = for {
//    len <- 1 to dice.length
//    combinations <- dice combinations len
//  } yield combinations
//  println(combos)
//
//
//  def betsToPayout(bets: List[PlacedBet], dice: List[Int]): Int = {
//
//    val combos = for {
//      len <- 1 to dice.length
//      combinations <- dice combinations len
//    } yield combinations
//    0
//  }
//}

object Bet {
//  final case object Small extends Bet(1, 1)
//  final case object Big extends Bet(2, 1)
//
//  final case object Odd extends Bet(3, 1)
//  final case object Even extends Bet(4, 1)

//  final case object Single1 extends Bet(5, 1)
//  final case object Single2 extends Bet(6, 1)
//  final case object Single3 extends Bet(7, 1)
//  final case object Single4 extends Bet(8, 1)
//  final case object Single5 extends Bet(9, 1)
//  final case object Single6 extends Bet(10, 1)
//
//  final case object Combo12 extends Bet(11, 5)
//  final case object Combo13 extends Bet(12, 5)
//  final case object Combo14 extends Bet(13, 5)
//  final case object Combo15 extends Bet(14, 5)
//  final case object Combo16 extends Bet(15, 5)
//  final case object Combo23 extends Bet(16, 5)
//  final case object Combo24 extends Bet(17, 5)
//  final case object Combo25 extends Bet(18, 5)
//  final case object Combo26 extends Bet(19, 5)
//  final case object Combo34 extends Bet(20, 5)
//  final case object Combo35 extends Bet(21, 5)
//  final case object Combo36 extends Bet(22, 5)
//  final case object Combo45 extends Bet(23, 5)
//  final case object Combo46 extends Bet(24, 5)
//  final case object Combo56 extends Bet(25, 5)

  final case object Total4 extends Bet(26, 50)
  final case object Total5 extends Bet(27, 20)
  final case object Total6 extends Bet(28, 15)
  final case object Total7 extends Bet(29, 12)
  final case object Total8 extends Bet(30, 8)
  final case object Total9 extends Bet(31, 6)
  final case object Total10 extends Bet(32, 6)
  final case object Total11 extends Bet(33, 6)
  final case object Total12 extends Bet(34, 6)
  final case object Total13 extends Bet(35, 8)
  final case object Total14 extends Bet(36, 12)
  final case object Total15 extends Bet(37, 15)
  final case object Total16 extends Bet(38, 20)
  final case object Total17 extends Bet(39, 50)

//  final case object Double1 extends Bet(40, 8)
//  final case object Double2 extends Bet(41, 8)
//  final case object Double3 extends Bet(42, 8)
//  final case object Double4 extends Bet(43, 8)
//  final case object Double5 extends Bet(44, 8)
//  final case object Double6 extends Bet(45, 8)
//
//  final case object TripleAny extends Bet(46, 30)
//
//  final case object Triple1 extends Bet(47, 150)
//  final case object Triple2 extends Bet(48, 150)
//  final case object Triple3 extends Bet(49, 150)
//  final case object Triple4 extends Bet(50, 150)
//  final case object Triple5 extends Bet(51, 150)
//  final case object Triple6 extends Bet(52, 150)

}




