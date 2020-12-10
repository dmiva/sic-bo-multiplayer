package com.dmiva.sicbo

import com.dmiva.sicbo.common.IncomingMessage
import com.dmiva.sicbo.common.codecs.IncomingMessageCodecs.incomingMessageOps
import com.dmiva.sicbo.domain.{Bet, BetType}

object Requests {
  def Login(username: String): String     = IncomingMessage.Login(username).toText
  def Logout(username: String): String    = IncomingMessage.Logout(username).toText
  def PlaceBet(bets: List[Bet]): String   = IncomingMessage.PlaceBet(bets).toText
}
