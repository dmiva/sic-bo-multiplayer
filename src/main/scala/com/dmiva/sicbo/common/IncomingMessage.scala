package com.dmiva.sicbo.common

import io.circe.generic.extras.ConfiguredJsonCodec
import JsonConfig.customConfig

// Messages from client to server
@ConfiguredJsonCodec sealed trait IncomingMessage

case class Login(username: String) extends IncomingMessage
case class PlaceBet(betId: Int, amount: Int) extends IncomingMessage