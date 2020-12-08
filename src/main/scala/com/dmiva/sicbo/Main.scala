package com.dmiva.sicbo

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.dmiva.sicbo.actors.GameRoom

import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()
    import system.dispatcher

    val interface = "127.0.0.1"
    val port = 9000
    val service = new WebService()

    val binding = Http().newServerAt(interface, port).bind(service.routes)
    binding.onComplete {
      case Success(binding) =>
        val localAddress = binding.localAddress
        println(s"Server started at ${localAddress.getHostName}:${localAddress.getPort}")
      case Failure(ex) =>
        println(s"Binding failed. Reason: ${ex.getMessage}")
        system.terminate()
    }

    sys.addShutdownHook {
      binding.flatMap(_.terminate(hardDeadline = 3.seconds)).flatMap { _ =>
        system.terminate()
      }
    }

  }
}
