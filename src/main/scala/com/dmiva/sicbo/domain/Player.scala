package com.dmiva.sicbo.domain

import com.dmiva.sicbo.common.CborSerializable

final case class Player(
                         id: Long,
                         username: Name,
                         password: Password,
                         userType: UserType,
                         balance: Balance
                       ) extends CborSerializable
