package com.dmiva.sicbo.repository

import com.dmiva.sicbo.domain.{Name, Player}
import com.dmiva.sicbo.repository.tables.PlayersTable
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.Future

class PlayerRepository(db: Database)
  extends Repository[Future]
  with PlayersTable {

  override def createSchema(): Future[Unit] =
    db.run(
      players.schema.createIfNotExists
    )

  override def insert(player: Player): Future[Int] =
    db.run(
      players += player
    )

  override def selectByName(name: Name): Future[Option[Player]] =
    db.run(
      players.filter(_.username === name).take(1).result.headOption
    )

  override def updateByName(name: Name, player: Player): Future[Int] =
    db.run(
      players.filter(_.username === name)
        .map(p => (p.balance))
        .update((player.balance))
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
