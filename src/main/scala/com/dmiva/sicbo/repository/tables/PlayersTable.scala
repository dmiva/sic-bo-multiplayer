package com.dmiva.sicbo.repository.tables

import com.dmiva.sicbo.domain.{Balance, Name, Password, Player, UserType}
import slick.ast.BaseTypedType
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.JdbcType
import slick.lifted.{ProvenShape, Rep}

trait PlayersTable {

  implicit val userTypeColumnType: JdbcType[UserType] with BaseTypedType[UserType] =
    MappedColumnType.base[UserType, String](
      userType => userType match {
        case UserType.User => "user"
        case UserType.Admin => "admin"
      },
      strValue => strValue match {
        case "user" => UserType.User
        case "admin" => UserType.Admin
      })
  implicit val balanceColumnType: JdbcType[Balance] with BaseTypedType[Balance] =
    MappedColumnType.base[Balance, BigDecimal](_.amount, Balance)


  final class Players(tag: Tag) extends Table[Player](tag, "players") {

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def username: Rep[Name] = column[Name]("username")
    def password: Rep[Password] = column[Password]("password")
    def userType: Rep[UserType] = column[UserType]("usertype")
    def balance: Rep[Balance] = column[Balance]("balance")

    override def * : ProvenShape[Player] = (id, username, password, userType, balance).mapTo[Player]
  }

  val players = TableQuery[Players]

}
