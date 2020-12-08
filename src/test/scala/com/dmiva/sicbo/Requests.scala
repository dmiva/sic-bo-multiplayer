package com.dmiva.sicbo

import com.dmiva.sicbo.common.IncomingMessage
import com.dmiva.sicbo.common.codecs.IncomingMessageCodecs.incomingMessageOps

object Requests {
  def Login(username: String): String             = IncomingMessage.Login(username).toText
  def Logout(username: String): String            = IncomingMessage.Logout(username).toText
  def PlaceBet(betId: Int, amount: Int): String   = IncomingMessage.PlaceBet(betId, amount).toText
}
