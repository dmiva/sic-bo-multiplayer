package com.dmiva.sicbo.common

import io.circe.generic.extras.Configuration

object JsonConfig {

  implicit val customConfig: Configuration =
    Configuration
      .default
      .withDiscriminator("$type")
      .withSnakeCaseConstructorNames
}
