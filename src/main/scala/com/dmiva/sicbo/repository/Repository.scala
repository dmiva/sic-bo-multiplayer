package com.dmiva.sicbo.repository

import com.dmiva.sicbo.domain.{Name, Player}

trait Repository[F[_]] {

  def createSchema(): F[Unit]

  def insert(player: Player): F[Player]

  def selectByName(name: Name): F[Option[Player]]

  def updateByName(name: Name, player: Player): F[Int]

  def deleteByName(name: Name): F[Int]

  def existsByName(name: Name): F[Boolean]
}
