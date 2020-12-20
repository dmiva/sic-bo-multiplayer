package com.dmiva.sicbo.common

import com.dmiva.sicbo.domain.{Balance, BetType, DiceOutcome, GamePhase, Name, PlayerInfo}
import io.circe.Printer

// Messages from server to client
sealed trait OutgoingMessage

object OutgoingMessage {
  case object RegistrationSuccessful extends OutgoingMessage
  case class LoginSuccessful(playerInfo: PlayerInfo) extends OutgoingMessage
  case object LoginFailed extends OutgoingMessage
  case object LoggedOut extends OutgoingMessage
  case object BetAccepted extends OutgoingMessage
  case class BetRejected(reason: String) extends OutgoingMessage
  case class Error(text: String) extends OutgoingMessage
  case class Ok(text: String) extends OutgoingMessage
  case class GameResult(
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
        s"$username bet amount $totalBet, won $totalWin. New balance: ${balance.amount}"
    }
  }

  case class GamePhaseChanged(newPhase: GamePhase) extends OutgoingMessage

  implicit val customPrinter: Printer = Printer.spaces2.copy(dropNullValues = true)
}

sealed trait BetRejectReason
object BetRejectReason {
  val NotEnoughBalance = "Not enough balance"
  val TimeExpired = "Wait for the next round"
  val BetAmountNotPositive = "Bet amount not positive"
  val MustBeAtLeastOneBet = "Must be at least one bet"
  def PlayerNotInGame(name: String) = s"Player $name is not in game"
}

sealed trait ErrorMessage
object ErrorMessage {
  val NotLoggedIn = "User is not logged in"
  val AlreadyLoggedIn = "Login attempt when already logged in"
  val InvalidRequest = "Invalid request"
}

