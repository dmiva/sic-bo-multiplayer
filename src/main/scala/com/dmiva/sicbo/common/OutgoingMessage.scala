package com.dmiva.sicbo.common

import com.dmiva.sicbo.domain.{BetType, DiceOutcome, GamePhase}
import com.dmiva.sicbo.domain.Player.{Balance, Name, PlayerInfo}

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
                         totalWin: BigDecimal,
                         balance: Balance,
                         username: Name
  ) extends OutgoingMessage

  case class GamePhaseChanged(newPhase: GamePhase) extends OutgoingMessage

}

sealed trait BetRejectReason
object BetRejectReason {
  val NotEnoughBalance = "Not enough balance"
  val TimeExpired = "Wait for the next round"
  def PlayerNotInGame(name: String) = s"Player $name is not in game"
}

sealed trait ErrorMessage
object ErrorMessage {
  val NotLoggedIn = "User is not logged in"
  val AlreadyLoggedIn = "Login attempt when already logged in"
  val InvalidRequest = "Invalid request"
}

