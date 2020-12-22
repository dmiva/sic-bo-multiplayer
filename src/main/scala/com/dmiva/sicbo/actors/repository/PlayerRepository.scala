package com.dmiva.sicbo.actors.repository

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.persistence.{PersistentActor, SaveSnapshotFailure, SaveSnapshotSuccess, SnapshotOffer}
import com.dmiva.sicbo.actors.repository.PlayerRepository.Command.{Login, Login2, LoginRequestResult, Register, UpdateBalance, UpdateBalance2, UpdateBalanceResult}
import com.dmiva.sicbo.actors.repository.PlayerRepository.RegistrationResult._
import PlayerRepository.{Event, LoginResult}
import akka.pattern.pipe
import com.dmiva.sicbo.common.IncomingMessage
import com.dmiva.sicbo.common.OutgoingMessage.{Error, RegistrationSuccessful}
import com.dmiva.sicbo.domain.{Balance, Name, Password, Player, UserType}
import com.dmiva.sicbo.service.PlayerService
import slick.util.SQLBuilder.Result

import scala.concurrent.ExecutionContext.Implicits.global

object PlayerRepository {

  /** All the commands that the PlayerRepository persistent actor supports. */
  sealed trait Command extends CborSerializable
  object Command {
    case class Register(username: Name, password: Password) extends Command
    case class Login(username: Name, password: Password) extends Command
    case class Login2(username: Name, password: Password) extends Command
    case class UpdateBalance(username: Name, balance: Balance) extends Command
    case class UpdateBalance2(username: Name, balance: Balance) extends Command
    case class UpdateBalanceResult(int: Int, replyTo: ActorRef) extends Command
    case class LoginRequestResult(player: Option[Player], password: Password, replyTo: ActorRef) extends Command
  }

  /** All the events that the PlayerRepository supports. */
  sealed trait Event extends CborSerializable
  object Event {
    case class Registered(username: Name, player: Player) extends Event
    case class BalanceUpdated(username: Name, player: Player) extends Event
  }

  sealed trait RegistrationResult
  object RegistrationResult {
    case object Successful extends RegistrationResult
    case object UsernameIsBlank extends RegistrationResult
    case object PasswordTooShort extends RegistrationResult
    case object PlayerAlreadyExists extends RegistrationResult

    implicit def convertToString(msg: RegistrationResult): String = msg.toString
  }

  sealed trait LoginResult
  object LoginResult {
    case class Successful(player: Player) extends LoginResult
    case object UserDoesNotExist extends LoginResult
    case object PasswordIncorrect extends LoginResult

    implicit def convertToString(msg: LoginResult): String = msg.toString
  }

  def props(service: PlayerService): Props = Props(new PlayerRepository(service))
  val persistenceId = "player-repository-id-1"
}

class PlayerRepository(service: PlayerService) extends PersistentActor with ActorLogging {

  override def persistenceId: String = PlayerRepository.persistenceId

  var storage: PlayerStorage = PlayerStorage(Map.empty) // Map[Name, Player]

  def updateStorage(event: Event): Unit = {
    storage = storage.updated(event)
  }

  override def receiveRecover: Receive = {
    case evt: Event => updateStorage(evt)
    case SnapshotOffer(metadata, snapshot: PlayerStorage) => storage = snapshot
      log.info(s"Snapshot ${metadata.persistenceId} seqId=${metadata.sequenceNr} recovered successfully.")
  }

  val snapshotInterval = 5

  // 1. Actor receives commands (messages)
  override def receiveCommand: Receive = {
    case SaveSnapshotSuccess(metadata)         =>
      log.info(s"Snapshot for ${metadata.persistenceId} seqId=${metadata.sequenceNr} saved successfully.")
    case SaveSnapshotFailure(metadata, reason) =>
      log.error(s"Snapshot for ${metadata.persistenceId} seqId=${metadata.sequenceNr} failure. Reason: ${reason.getMessage}")
//    case Register(name, pw) => {
//      val replyTo = sender()
//      val result = (name, pw) match {
//        case (name, _)  if name.isBlank                    => Left(Error(UsernameIsBlank))
//        case (_, pw)    if pw.length < 4                   => Left(Error(PasswordTooShort))
//        case (name, _)  if storage.isPlayerRegistered(name)  => Left(Error(PlayerAlreadyExists))
//        case success                                       => Right(success)
//      }
//      result match {
//        // 2. If command fails to execute (negative outcome) then the sender is informed about that
//        // Event is not persisted in this case
//        case Left(error) => replyTo ! error
//        case Right((name, pw)) =>
//          // 3. If command succeeds, then an event is generated, and persisted
//          // 4. The generated event is then internally sent back to this actor and handled
//          persist(Event.Registered(name, Player(0, name, pw, UserType.User, Balance(100))))(event => handleEvent(event, replyTo)) // TODO: Implement Id
//      }
//    }
//    case Login(name, pw) => {
//      val replyTo = sender()
//      val player = storage.getPlayerByName(name)
//      val loginResult = player match {
//        case Some(player) if player.password == pw  => LoginResult.Successful(player)
//        case Some(_)                                => LoginResult.PasswordIncorrect
//        case None                                   => LoginResult.UserDoesNotExist
//      }
//      replyTo ! loginResult
//    }


//    case UpdateBalance(name, balance) => {
//      val replyTo = sender()
//      val player = storage.getPlayerByName(name)
//      player match {
//        case Some(player) => persist(Event.BalanceUpdated(name, player.copy(balance = balance)))(event => handleEvent(event, replyTo))
//        case None =>
//      }
//    }


    // Login through database
    case Login2(name, pw) => {
      val replyTo = sender()
      val futureResult = service.loginPlayer(IncomingMessage.Login(name, pw))
        .map(result => LoginRequestResult(result, pw, replyTo))
      futureResult pipeTo self
    }
    case LoginRequestResult(player, pw, replyTo) => {
      val loginResult = player match {
        case Some(player) if player.password == pw  => LoginResult.Successful(player)
        case Some(_)                                => LoginResult.PasswordIncorrect
        case None                                   => LoginResult.UserDoesNotExist
      }
      replyTo ! loginResult
    }
    // Update through database
    case UpdateBalance2(name, balance) => {
      val replyTo = sender()
      val futureResult = service.updateBalance(name, balance)
        .map(result => UpdateBalanceResult(result, replyTo))
      futureResult pipeTo self
    }
    case UpdateBalanceResult(numOfRowsUpdated, replyTo) => {
      numOfRowsUpdated match {
        case 0 => println("Updated 0 rows")
        case 1 => println("Updated 1 rows")
      }
    }


  }


//  def handleEvent(event: Event, replyTo: ActorRef): Unit = {
//    // 5. Mutating the state of the actor
//    updateStorage(event)
//    context.system.eventStream.publish(event)
//    // 6. Reply to the sender of the command about success
//    event match {
//      case Event.Registered(_,_) => replyTo ! RegistrationSuccessful
//      case Event.BalanceUpdated(_,_) => () // do nothing else TODO: Send player info to player
//    }
//    if (lastSequenceNr % snapshotInterval == 0 && lastSequenceNr != 0)
//      saveSnapshot(storage)
//  }

}