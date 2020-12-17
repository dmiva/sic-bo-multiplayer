package com.dmiva.sicbo.actors.repository

import akka.actor.{ActorRef, Props}
import akka.persistence.{PersistentActor, SnapshotOffer}
import com.dmiva.sicbo.actors.repository.PlayerRepository.Command.{Login, Register, UpdateBalance}
import com.dmiva.sicbo.actors.repository.PlayerRepository.RegistrationResult._
import PlayerRepository.{Event, LoginResult}
import com.dmiva.sicbo.actors.repository.PlayerRepository.LoginResult.PasswordIncorrect
import com.dmiva.sicbo.common.OutgoingMessage.{Error, RegistrationSuccessful}
import com.dmiva.sicbo.domain.Player.{Balance, Name, Password, Player, UserType}

object PlayerRepository {

  sealed trait Command
  object Command {
    case class Register(username: Name, password: Password) extends Command
    case class Login(username: Name, password: Password) extends Command
    case class UpdateBalance(username: Name, balance: Balance) extends Command
  }

  sealed trait Event
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

  def props(): Props = Props[PlayerRepository]
  val persistenceId = "user-repository-id-1"
}

class PlayerRepository extends PersistentActor {

  override def persistenceId: String = PlayerRepository.persistenceId

  var storage: PlayerStorage = PlayerStorage(Map.empty)

  def updateStorage(event: Event): Unit = {
    storage = storage.updated(event)
  }

  override def receiveRecover: Receive = {
    case evt: Event => updateStorage(evt)
    case SnapshotOffer(_, snapshot: PlayerStorage) => storage = snapshot
  }

  val snapshotInterval = 5

  // 1. Actor receives commands (messages)
  override def receiveCommand: Receive = {
    case Register(name, pw) => {
      val replyTo = sender()
      val result = (name, pw) match {
        case (name, _)  if name.isBlank                    => Left(Error(UsernameIsBlank))
        case (_, pw)    if pw.length < 4                   => Left(Error(PasswordTooShort))
        case (name, _)  if storage.isPlayerRegistered(name)  => Left(Error(PlayerAlreadyExists))
        case success                                       => Right(success)
      }
      result match {
        // 2. If command fails to execute (negative outcome) then the sender is informed about that
        // Event is not persisted in this case
        case Left(error) => replyTo ! error
        case Right((name, pw)) =>
          // 3. If command succeeds, then an event is generated, and persisted
          // 4. The generated event is then internally sent back to this actor and handled
          persist(Event.Registered(name, Player(0, name, pw, UserType.User, Balance(100))))(event => handleEvent(event, replyTo)) // TODO: Implement Id
      }
    }
    case Login(name, pw) => {
      val replyTo = sender()
      val player = storage.getPlayerByName(name)
      val loginResult = player match {
        case Some(player) if player.password == pw  => LoginResult.Successful(player)
        case Some(_)                                => LoginResult.PasswordIncorrect
        case None                                   => LoginResult.UserDoesNotExist
      }
      replyTo ! loginResult
    }
    case UpdateBalance(name, balance) => {
      val replyTo = sender()
      val player = storage.getPlayerByName(name)
      player match {
        case Some(player) => persist(Event.BalanceUpdated(name, player.copy(balance = balance)))(event => handleEvent(event, replyTo))
        case None =>
      }

    }


  }


  def handleEvent(event: Event, replyTo: ActorRef): Unit = {
    // 5. Mutating the state of the actor
    updateStorage(event)
    context.system.eventStream.publish(event)
    // 6. Reply to the sender of the command about success
    event match {
      case Event.Registered(_,_) => replyTo ! RegistrationSuccessful
      case Event.BalanceUpdated(_,_) => () // do nothing else
    }
    if (lastSequenceNr % snapshotInterval == 0 && lastSequenceNr != 0)
      saveSnapshot(storage)
  }

}