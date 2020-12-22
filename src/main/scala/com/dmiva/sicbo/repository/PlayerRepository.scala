package com.dmiva.sicbo.repository

import cats.data.OptionT
import com.dmiva.sicbo.domain.{Balance, Name, Player}
import com.dmiva.sicbo.repository.tables.PlayersTable
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class PlayerRepository(db: Database)(implicit ec: ExecutionContext)
  extends Repository[Future]
  with PlayersTable {

  override def createSchema(): Future[Unit] =
    db.run(
      players.schema.createIfNotExists
    )

  override def insert(player: Player): Future[Player] =
    db.run(
      players returning players.map(_.id) += player
    ).map(id => player.copy(id = id))

  override def selectByName(name: Name): Future[Option[Player]] =
    db.run(
      players.filter(_.username === name).take(1).result.headOption
    )

  def findByName(name: Name): OptionT[Future, Player] =
    OptionT(db.run(
      players.filter(_.username === name).take(1).result.headOption
    ))

  override def updateByName(name: Name, balance: Balance): Future[Int] =
    db.run(
      players.filter(_.username === name)
        .map(p => (p.balance))
        .update(balance)
    )

  override def deleteByName(name: Name): Future[Int] =
    db.run(
      players.filter(_.username === name).delete
    )

  override def existsByName(name: Name): Future[Boolean] =
    db.run(
      players.filter(_.username === name).exists.result
    )

}
