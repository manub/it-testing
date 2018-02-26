lazy val commonSettings = Seq(
  name := "it-testing",
  version := "0.1",
  organization := "net.manub",
  scalaVersion := "2.12.4"
)

val http4sVersion = "0.17.5"
val doobieVersion = "0.4.4"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "io.circe" %% "circe-generic" % "0.8.0",
  "org.tpolecat" %% "doobie-core-cats" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres-cats" % doobieVersion,
  "org.flywaydb" % "flyway-core" % "4.2.0",
  "com.typesafe" % "config" % "1.3.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",

  "com.whisk" %% "docker-testkit-scalatest" % "0.9.5" % IntegrationTest,
  "com.whisk" %% "docker-testkit-impl-spotify" % "0.9.5" % IntegrationTest
)

lazy val root = (project in file(".")).configs(IntegrationTest).settings(commonSettings, Defaults.itSettings)