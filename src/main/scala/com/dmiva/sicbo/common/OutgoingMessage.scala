package com.dmiva.sicbo.common

import com.dmiva.sicbo.domain.{Balance, BetType, DiceOutcome, GamePhase, Name, PlayerInfo}
import io.circe.Printer

// Messages from server to client
sealed trait OutgoingMessage

object OutgoingMessage {
  case object RegistrationSuccessful extends OutgoingMessage
  final case class LoginSuccessful(playerInfo: PlayerInfo) extends OutgoingMessage
  case object LoginFailed extends OutgoingMessage
  case object LoggedOut extends OutgoingMessage
  case object BetAccepted extends OutgoingMessage
  final case class BetRejected(reason: String) extends OutgoingMessage
  final case class Error(text: String) extends OutgoingMessage
  final case class Ok(text: String) extends OutgoingMessage
  final case class GamePhaseChanged(newPhase: GamePhase) extends OutgoingMessage
  final case class GameResult(
                         diceOutcome: DiceOutcome,
                         winningBetTypes: List[BetType],
                         totalBet: BigDecimal,
                         totalWin: BigDecimal,
                         balance: Balance,
                         username: Name
  ) extends OutgoingMessage {
    override def toString: String = {
        s"Dice outcome: ${diceOutcome.a} ${diceOutcome.b} ${diceOutcome.c} " +
        s"TOTAL ${diceOutcome.a + diceOutcome.b + diceOutcome.c}. " +
        s"Winning bets: $winningBetTypes. " +
        s"$username bet amount $totalBet, won $totalWin. New balance: ${balance.amount}\r\n"
    }
  }

  implicit val customPrinter: Printer = Printer.spaces2.copy(dropNullValues = true)
}

