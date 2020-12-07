package com.dmiva.sicbo

import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.stream.scaladsl.Flow

class WebService() extends Directives {
  val authRoute =
    get {
      pathSingleSlash {
        complete("Welcome to SicBo server")
      }
    }

  val flow: Flow[Message, Message, _] =
    Flow[Message].collect {
      case TextMessage.Strict(msg) => msg
    }
      .map {
        case msg: String => TextMessage.Strict(msg)
      }

  val webSocketRoute =
    path("game") {
      get {
        handleWebSocketMessages(flow)
      }
    }

  val route = authRoute ~ webSocketRoute

}
