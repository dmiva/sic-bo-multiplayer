package com.dmiva.sicbo.service

import cats.data.EitherT
import com.dmiva.sicbo.common.IncomingMessage.{Login, Register}
import com.dmiva.sicbo.domain.{Balance, Name, Player, UserType}
import com.dmiva.sicbo.repository.PlayerRepository
import com.dmiva.sicbo.service.PlayerService.initialBalance

import scala.concurrent.{ExecutionContext, Future}

final case class PlayerAlreadyExistsError(player: Player)
final case class PlayerNotFoundError()
final case class PasswordIsIncorrect()

object PlayerService {
  val initialBalance = BigDecimal(100)
}

class PlayerService(playerRepository: PlayerRepository)(implicit ec: ExecutionContext) {

  def playerDoesNotExist(name: Name): EitherT[Future, PlayerAlreadyExistsError, Unit] = {
    playerRepository.findByName(name).map(PlayerAlreadyExistsError).toLeft(())
  }

  def getPlayerByName(name: String): EitherT[Future, PlayerNotFoundError, Player] =
    playerRepository.findByName(name).toRight(PlayerNotFoundError())

  def registerPlayer(reg: Register): EitherT[Future, PlayerAlreadyExistsError, Player] = {
    for {
      _ <- playerDoesNotExist(reg.username)
      player <- EitherT.liftF(playerRepository.insert(Player(0, reg.username, reg.password, UserType.User, Balance(initialBalance))))
    } yield player
  }

  def loginPlayer(login: Login): Future[Option[Player]] = {
    playerRepository.selectByName(login.username)
  }

  def updateBalance(name: Name, balance: Balance): Future[Int] = {
    playerRepository.updateByName(name, balance)
  }

}
