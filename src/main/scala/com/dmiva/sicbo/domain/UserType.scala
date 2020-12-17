package com.dmiva.sicbo.domain

import com.fasterxml.jackson.core.{JsonGenerator, JsonParser}
import com.fasterxml.jackson.databind.{DeserializationContext, SerializerProvider}
import com.fasterxml.jackson.databind.annotation.{JsonDeserialize, JsonSerialize}
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer

@JsonSerialize(using = classOf[UserTypeJsonSerializer])
@JsonDeserialize(using = classOf[UserTypeJsonDeserializer])
sealed trait UserType
object UserType {
  final case object User extends UserType
  final case object Admin extends UserType
}

/** Used in Akka serialization (snapshots) */
class UserTypeJsonSerializer extends StdSerializer[UserType](classOf[UserType]) {
  import UserType._

  override def serialize(value: UserType, gen: JsonGenerator, provider: SerializerProvider): Unit = {
    val strValue = value match {
      case User => "user"
      case Admin  => "admin"
    }
    gen.writeString(strValue)
  }
}

/** Used in Akka serialization (snapshots) */
class UserTypeJsonDeserializer extends StdDeserializer[UserType](classOf[UserType]) {
  import UserType._

  override def deserialize(p: JsonParser, ctxt: DeserializationContext): UserType = {
    p.getText match {
      case "user" => User
      case "admin" => Admin
    }
  }
}
