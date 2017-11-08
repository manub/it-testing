name := "it-testing"

version := "0.1"

organization := "net.manub"

scalaVersion := "2.12.4"


val http4sVersion = "0.17.5"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
)