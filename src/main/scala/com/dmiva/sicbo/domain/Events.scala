package com.dmiva.sicbo.domain

trait EventType
//object EventType {
//  final case class
//}
object EventType {
  final case object BetPlaced
}

trait Result
object Result {
  final case object Accepted
  final case object Rejected
}

case class Event(eventType: EventType) {

}

object Messages {
  // From player to room
  object Player {
    case class BetPlaced(betId: Int, amount: Int)
  }

  object Game {
    // From room to player
    object BetPlaceResult {
      final case object Accepted
      final case object Rejected
    }
  }
}