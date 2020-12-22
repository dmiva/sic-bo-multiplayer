package com.dmiva.sicbo.service

import cats.data.EitherT
import com.dmiva.sicbo.common.IncomingMessage.{Login, Register}
import com.dmiva.sicbo.domain.{Balance, Name, Player, UserType}
import com.dmiva.sicbo.repository.PlayerRepository

import scala.concurrent.{ExecutionContext, Future}

case class PlayerAlreadyExistsError(player: Player)
case class PlayerNotFoundError()
case class PasswordIsIncorrect()

class PlayerService(repo: PlayerRepository)(implicit ec: ExecutionContext) {

  def playerDoesNotExist(name: Name): EitherT[Future, PlayerAlreadyExistsError, Unit] = {
    repo.findByName(name).map(PlayerAlreadyExistsError).toLeft(())
  }

  def getPlayerByName(name: String): EitherT[Future, PlayerNotFoundError, Player] =
    repo.findByName(name).toRight(PlayerNotFoundError())

  def registerPlayer(reg: Register): EitherT[Future, PlayerAlreadyExistsError, Player] = {
    for {
      _ <- playerDoesNotExist(reg.username)
      player <- EitherT.liftF(repo.insert(Player(0, reg.username, reg.password, UserType.User, Balance(100))))
    } yield player
  }

  def loginPlayer(login: Login): Future[Option[Player]] = {
    repo.selectByName(login.username)
  }

  def updateBalance(name: Name, balance: Balance): Future[Int] = {
    repo.updateByName(name, balance)
  }



}
