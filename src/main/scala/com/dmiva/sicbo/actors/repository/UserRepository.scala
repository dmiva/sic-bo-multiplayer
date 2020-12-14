package com.dmiva.sicbo.actors.repository

import akka.actor.{ActorRef, Props}
import akka.persistence.{PersistentActor, SnapshotOffer}
import com.dmiva.sicbo.actors.repository.UserRepository.Command.{Login, Register}
import com.dmiva.sicbo.actors.repository.UserRepository.RegistrationResult._
import UserRepository.{Event, LoginResult}
import com.dmiva.sicbo.actors.repository.UserRepository.LoginResult.PasswordIncorrect
import com.dmiva.sicbo.common.OutgoingMessage.{Error, Ok, RegistrationSuccessful}
import com.dmiva.sicbo.domain.Player.{Name, Password, User, UserType}

object UserRepository {

  sealed trait Command
  object Command {
    case class Register(username: Name, password: Password) extends Command
    case class Login(username: Name, password: Password) extends Command
  }

  sealed trait Event
  object Event {
    case class Registered(username: Name, user: User) extends Event
  }

  sealed trait RegistrationResult
  object RegistrationResult {
    case object Successful extends RegistrationResult
    case object UsernameIsBlank extends RegistrationResult
    case object PasswordTooShort extends RegistrationResult
    case object UserAlreadyExists extends RegistrationResult

    implicit def convertToString(msg: RegistrationResult): String = msg.toString
  }

  sealed trait LoginResult
  object LoginResult {
    case class Successful(user: User) extends LoginResult
    case object UserDoesNotExist extends LoginResult
    case object PasswordIncorrect extends LoginResult

    implicit def convertToString(msg: LoginResult): String = msg.toString
  }

  def props(): Props = Props[UserRepository]
  val persistenceId = "user-repository-id-1"
}

class UserRepository extends PersistentActor {

  override def persistenceId: String = UserRepository.persistenceId

  var storage: UserStorage = UserStorage(Map.empty)

  def updateStorage(event: Event): Unit = {
    storage = storage.updated(event)
  }

  override def receiveRecover: Receive = {
    case evt: Event => updateStorage(evt)
    case SnapshotOffer(_, snapshot: UserStorage) => storage = snapshot
  }

  val snapshotInterval = 5

  // 1. Actor receives commands (messages)
  override def receiveCommand: Receive = {
    case Register(name, pw) => {
      val replyTo = sender()
      val result = (name, pw) match {
        case (name, _)  if name.isBlank                    => Left(Error(UsernameIsBlank))
        case (_, pw)    if pw.length < 4                   => Left(Error(PasswordTooShort))
        case (name, _)  if storage.isUserRegistered(name)  => Left(Error(UserAlreadyExists))
        case success                                       => Right(success)
      }
      result match {
        // 2. If command fails to execute (negative outcome) then the sender is informed about that
        // Event is not persisted in this case
        case Left(error) => replyTo ! error
        case Right((name, pw)) =>
          // 3. If command succeeds, then an event is generated, and persisted
          // 4. The generated event is then internally sent back to this actor and handled
          persist(Event.Registered(name, User(0, name, pw, UserType.User)))(event => handleEvent(event, replyTo)) // TODO: Implement Id
      }
    }
    case Login(name, pw) => {
      val replyTo = sender()
      val user = storage.getUserByName(name)
      val loginResult = user match {
        case Some(user) if user.password == pw  => LoginResult.Successful(user)
        case Some(_)                            => LoginResult.PasswordIncorrect
        case None                               => LoginResult.UserDoesNotExist
      }
      replyTo ! loginResult
    }

  }


  def handleEvent(event: Event, replyTo: ActorRef): Unit = {
    // 5. Mutating the state of the actor
    updateStorage(event)
    context.system.eventStream.publish(event)
    // 6. Reply to the sender of the command about success
    event match {
      case Event.Registered(_,_) => replyTo ! RegistrationSuccessful
    }
    if (lastSequenceNr % snapshotInterval == 0 && lastSequenceNr != 0)
      saveSnapshot(storage)
  }

}