package com.dmiva.sicbo
import com.dmiva.sicbo.common.OutgoingMessage.LoginFailed
import com.dmiva.sicbo.common.{ErrorMessage, OutgoingMessage}
import com.dmiva.sicbo.common.codecs.OutgoingMessageCodecs.outgoingMessageOps
import com.dmiva.sicbo.domain.{GamePhase, PlayerInfo}

/**
 * Encapsulates all responses to the client.<p>
 * Used for automated test purposes
 */
object Responses {
  val RegistrationSuccessful: String       = OutgoingMessage.RegistrationSuccessful.toText
  def LoginSuccessful(playerInfo: PlayerInfo): String       = OutgoingMessage.LoginSuccessful(playerInfo).toText
  val LoginFailed: String           = OutgoingMessage.LoginFailed.toText
  val LoggedOut: String             = OutgoingMessage.LoggedOut.toText
  val BetAccepted: String           = OutgoingMessage.BetAccepted.toText
  val ErrorNotLoggedIn: String      = OutgoingMessage.Error(ErrorMessage.NotLoggedIn).toText
  val ErrorAlreadyLoggedIn: String  = OutgoingMessage.Error(ErrorMessage.AlreadyLoggedIn).toText
  val ErrorInvalidRequest: String       = OutgoingMessage.Error(ErrorMessage.InvalidRequest).toText
  def GamePhaseChanged(newPhase: GamePhase): String = OutgoingMessage.GamePhaseChanged(newPhase).toText
}
