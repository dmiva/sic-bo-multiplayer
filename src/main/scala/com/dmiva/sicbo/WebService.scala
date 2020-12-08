package com.dmiva.sicbo

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.dmiva.sicbo.actors.{GameRoom, User}
import com.dmiva.sicbo.common.{Error, IncomingMessage, OutgoingMessage}
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax.EncoderOps

import scala.concurrent.duration.DurationInt

class WebService(implicit val system: ActorSystem) extends Directives {
  val gameRoom = system.actorOf(GameRoom.props(), "game")


  val authRoute: Route =
    get {
      pathSingleSlash {
        complete("Welcome to SicBo server")
      }
    }

  private def newUserFlow: Flow[Message, Message, NotUsed] = {
    // Creating an actor for every WS connection
    val newUserActor = system.actorOf(User.props(gameRoom))
    // All WS packets are coming to this sink
    val sink: Sink[Message, NotUsed] =
      Flow[Message]
        .map {
          // Anonymous Flow is used to transform incoming message from JSON to domain message
          case TextMessage.Strict(message) => decode[IncomingMessage](message) match {
            case Right(request) => request
            case Left(error) => Error(s"Error processing request: ${error.getMessage}")
          }
          case _ => Error("Not supported")
        }
        // Sink is actor, which receives in mailbox all stream elements
        // When stream completes (e.g. disconnection), the last event is emitted to inform the actor
        .to(Sink.actorRef(newUserActor, User.Disconnected, _ => User.Disconnected))

    val source: Source[Message, NotUsed] =
      Source.actorRef[OutgoingMessage](
        PartialFunction.empty,
        PartialFunction.empty,
        bufferSize = 10, overflowStrategy = OverflowStrategy.fail)
        .map {
          message: OutgoingMessage => TextMessage.Strict(message.asJson.toString())
        }
        // wsOutput is handle that is used to send messages to WS
        // When wsOutput sends a message, it is emitted to websocket stream
        .mapMaterializedValue { wsOutput =>
          // Pass the handle to newly created actor, that is handling user connection
          newUserActor ! User.Connected(wsOutput)
          NotUsed
        }
        .keepAlive(maxIdle = 10.seconds, () => TextMessage.Strict("Keep-alive"))

    Flow.fromSinkAndSource(sink, source)
  }

  val webSocketRoute: Route =
    path("game") {
      get {
        handleWebSocketMessages(newUserFlow)
      }
    }

  val routes = authRoute ~ webSocketRoute

}
