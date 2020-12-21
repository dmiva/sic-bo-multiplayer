name := "sic-bo-multiplayer"

version := "0.1"

scalaVersion := "2.13.4"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-Ymacro-annotations",
  "-Xlint:unused",
  "-Wunused:implicits"
)

val akkaVersion = "2.6.10"
val akkaHttpVersion = "10.2.1"
val akkaHttpCirceVersion = "1.35.2"
val scalaTestVersion = "3.2.3"
val scalaTestPlusVersion = "3.1.0.0-RC2"
val scalaCheckVersion = "1.15.1"
val circeVersion = "0.13.0"
val slickVersion = "3.3.3"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  "org.scalacheck" %% "scalacheck" % scalaCheckVersion % Test,
  "org.scalatestplus" %% "scalatestplus-scalacheck" % scalaTestPlusVersion % Test,
  "org.mockito" %% "mockito-scala" % "1.16.3",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirceVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,
  "io.circe" %% "circe-optics" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "eu.timepit" %% "refined" % "0.9.19",
  "io.circe" %% "circe-refined" % circeVersion,
  "com.h2database" % "h2" % "1.4.200",
  "com.typesafe.slick" %% "slick" % slickVersion,
  "org.postgresql" % "postgresql" % "42.2.18",
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion

)

fork in run := true