package com.dmiva.sicbo

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.dmiva.sicbo.actors.Lobby
import com.dmiva.sicbo.repository.PlayerRepository
import com.dmiva.sicbo.service.PlayerService
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()
    import system.dispatcher

    val config = system.settings.config
    val interface = config.getString("app.interface")
    val port = config.getInt("app.port")
    val lobby = system.actorOf(Lobby.props(), "lobby")

    val db = Database.forConfig("database")
    val playerRepository: PlayerRepository = new PlayerRepository(db)
    val playerService: PlayerService = new PlayerService(playerRepository)
    playerRepository.createSchema().onComplete {
      case Failure(exception) =>
        println(s"Failed to create database schema. Exception: $exception")
        throw exception
      case Success(_) =>
        println(s"Player repository schema successfully created")
    }

    val httpRoutes = new HttpRoutes(lobby, playerService)

    val binding = Http().newServerAt(interface, port).bind(httpRoutes.routes)
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
