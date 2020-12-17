package com.dmiva.sicbo.domain

import akka.actor.ActorRef

case class PlayerSession(player: Player, ref: ActorRef)
