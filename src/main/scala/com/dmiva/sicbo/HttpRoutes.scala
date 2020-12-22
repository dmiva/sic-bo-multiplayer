package com.dmiva.sicbo

import akka.{Done, NotUsed}
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.{CompletionStrategy, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.dmiva.sicbo.RegistrationResult.PlayerAlreadyExists
import com.dmiva.sicbo.actors.User
import com.dmiva.sicbo.common.IncomingMessage.Register
import com.dmiva.sicbo.common.{IncomingMessage, OutgoingMessage}
import com.dmiva.sicbo.common.OutgoingMessage.Error
import com.dmiva.sicbo.common.codecs.IncomingMessageCodecs._
import com.dmiva.sicbo.common.codecs.OutgoingMessageCodecs._
import com.dmiva.sicbo.service.{PlayerAlreadyExistsError, PlayerService}
import io.circe.jawn.decode
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.syntax.EncoderOps

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

sealed trait RegistrationResult
object RegistrationResult {
  case object Successful extends RegistrationResult
  case object UsernameIsBlank extends RegistrationResult
  case object PasswordTooShort extends RegistrationResult
  case object PlayerAlreadyExists extends RegistrationResult

  implicit def convertToString(msg: RegistrationResult): String = msg.toString
}

class HttpRoutes(lobby: ActorRef, playerService: PlayerService)(implicit val system: ActorSystem, val executionContext: ExecutionContext) extends Directives {

  val helloRoute: Route =
    get {
      pathSingleSlash {
        complete("Welcome to SicBo server")
      }
    }

  val registerRoute: Route =
    (post & path("register")) {
      entity(as[Register]) { registerRequest =>
        val request = for {
          result <- playerService.registerPlayer(registerRequest)
        } yield result

        onComplete(request.value) {
          case Failure(ex) => complete(StatusCodes.InternalServerError)
          case Success(value) => value match {
            case Left(PlayerAlreadyExistsError(player)) => complete(
              HttpResponse(
                status = StatusCodes.Conflict,
                entity = HttpEntity(ContentTypes.`application/json`, Error(PlayerAlreadyExists).asJson.toString)
              )
            )
            case Right(player) => complete(HttpResponse(
              status = StatusCodes.Created,
              entity = HttpEntity(ContentTypes.`application/json`, player.asJson.toString
              )))
          }
        }
      }
    }

  private def newUserFlow: Flow[Message, Message, NotUsed] = {
    // Creating an actor for every WS connection
    val newUserActor = system.actorOf(User.props(lobby))
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

    val sourceCompletionMatcher: PartialFunction[Any, CompletionStrategy] = {
      case Done =>
        // complete stream immediately if we send it message Done
        CompletionStrategy.immediately
    }

    val source: Source[Message, NotUsed] =
      Source.actorRef[OutgoingMessage](
        completionMatcher = sourceCompletionMatcher,
        failureMatcher = PartialFunction.empty,
        bufferSize = 10,
        overflowStrategy = OverflowStrategy.fail)
        .map {
          message: OutgoingMessage => message.toTextMessage
        }
        // wsHandle is handle that is used to send messages to WS
        // When wsOutput sends a message, it is emitted to websockets stream
        .mapMaterializedValue { wsHandle =>
          // Pass the handle to newly created actor, that is handling user connection
          newUserActor ! User.Connected(wsHandle)
          NotUsed
        }
        .keepAlive(maxIdle = 20.seconds, () => TextMessage.Strict("Keep-alive"))

    Flow.fromSinkAndSource(sink, source)
  }

  val webSocketRoute: Route =
    path("game") {
      get {
        handleWebSocketMessages(newUserFlow)
      }
    }

  val routes: Route = helloRoute ~ webSocketRoute ~ registerRoute

}
