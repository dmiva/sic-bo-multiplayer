package com.dmiva.sicbo.actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes, Uri}
import akka.pattern.pipe
import akka.util.ByteString
import com.dmiva.sicbo.domain.DiceOutcome

import scala.concurrent.Future
import scala.util.Random
import scala.util.control.NonFatal

object DiceRoller {
  // Command
  case object RollDice
  // Event
  final case class DiceResult(dice: DiceOutcome)

  private final case class Result(response: HttpResponse, replyTo: ActorRef)

  def props() = Props(new DiceRoller)

  /** URL used to obtain True RNG values for dice */
  private val httpUrl = "https://www.random.org/integers/?num=3&min=1&max=6&col=1&base=10&format=plain&rnd=new"
}

/**
 * Actor that provides dice gameResult using true or pseudo RNG.
 * True RNG sends request to Random [dot] org and
 * in case if gameResult is as expected, it is used in the game.
 *
 * If by any reason True RNG fails, then
 * dice gameResult is obtained using built-in pseudo RNG.
 *
 * If by any reason Pseudo RNG fails, then [[IllegalStateException]] exception is thrown.
 */
class DiceRoller extends Actor with ActorLogging {
  import DiceRoller._

  implicit val system: ActorSystem = context.system
  import system.dispatcher

  private val http = Http(system)

  private[actors] def getTrueRandomNumber(uri: Uri): Future[HttpResponse] = { // TODO: Manage UnknownHostException
//    log.info("Obtaining true random values for dice...")
    http.singleRequest(HttpRequest(uri = uri)) recover {
      case NonFatal(t) => HttpResponse(StatusCodes.ServiceUnavailable)
    }
  }

  private def getPseudoRNGDiceResult(replyTo: ActorRef): Unit = {
//    log.info("Obtaining pseudo random values for dice...")
    val a = Random.nextInt(6) + 1
    val b = Random.nextInt(6) + 1
    val c = Random.nextInt(6) + 1
    DiceOutcome.from(a, b, c) match {
      case Some(dice) =>
        replyTo ! DiceResult(dice)
      case None =>
        log.error(s"Pseudo RNG gameResult failed to parse: a=$a, b=$b, c=$c")
        throw new IllegalStateException("Pseudo RNG gameResult failed to parse")
    }
  }

  override def receive: Receive = {

    case RollDice =>

      val replyTo = sender()
      val futureResult = getTrueRandomNumber(httpUrl)
        .map(result => Result(result, replyTo))

      futureResult pipeTo self

    case Result(response, replyTo) => response match {
      case HttpResponse(StatusCodes.OK, _, entity, _) =>

        entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
          // response is 6 bytes, which are 3 numbers, delimited with new line character
          // example: '4 \n 3 \n 5 \n'
          body.utf8String.split("\n").toList match {
            case a :: b :: c :: Nil =>
              DiceOutcome.from(a, b, c) match {
                case Some(dice) => replyTo ! DiceResult(dice)
                case None =>
                  log.error(s"True RNG gameResult failed to parse: a=$a, b=$b, c=$c, url=$httpUrl")
                  getPseudoRNGDiceResult(replyTo)
              }
            case _ =>
              log.error(s"True RNG returned unexpected gameResult: body=${body.utf8String}, url=$httpUrl")
              getPseudoRNGDiceResult(replyTo)
          }
        }

      case resp @ HttpResponse(code, _, _, _) =>
        log.error(s"True RNG failed to respond: HTTP StatusCode=$code, url=$httpUrl")
        resp.discardEntityBytes()
        getPseudoRNGDiceResult(replyTo)
    }
  }

}