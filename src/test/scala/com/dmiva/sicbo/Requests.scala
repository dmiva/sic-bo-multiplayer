package com.dmiva.sicbo

import com.dmiva.sicbo.common.IncomingMessage
import com.dmiva.sicbo.common.codecs.IncomingMessageCodecs.incomingMessageOps
import com.dmiva.sicbo.domain.{Bet}

object Requests {
  def Register(username: String, password: String): String     = IncomingMessage.Register(username, password).toText
  def Login(username: String, password: String): String     = IncomingMessage.Login(username, password).toText
  def Logout(username: String): String    = IncomingMessage.Logout(username).toText
  def PlaceBet(bets: List[Bet]): String   = IncomingMessage.PlaceBet(bets).toText
}
