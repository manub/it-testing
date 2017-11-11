package net.manub.ittesting

import com.spotify.docker.client.DefaultDockerClient
import com.typesafe.config.ConfigFactory
import com.whisk.docker.impl.spotify.{DockerKitSpotify, SpotifyDockerFactory}
import doobie.imports.DriverManagerTransactor
import fs2.Task
import org.scalatest.{Matchers, WordSpec}
import doobie.imports._
import cats._
import cats.data._
import cats.implicits._
import com.whisk.docker.scalatest.DockerTestKit
import fs2.interop.cats._
import org.flywaydb.core.Flyway
import org.scalatest.concurrent.ScalaFutures

class UsersRepositoryIntegrationSpec
    extends WordSpec
    with Matchers
    with ScalaFutures
    with DockerTestKit // scalatest integration
    with DockerKitSpotify // docker client implementation
    with DockerPostgresService { // container rules

  val config = ConfigFactory.load()
  val url = config.getString("database.url")
  val username = config.getString("database.username")
  val password = config.getString("database.password")

  val xa = DriverManagerTransactor[Task](
    "org.postgresql.Driver",
    url,
    username,
    password
  )

  val repository = new UsersRepository(config)

  "users repository" when {
    "getting all the users" should {
      "return all users on the database" in {

        val flyway = new Flyway()
        flyway.setDataSource(url, username, password)
        flyway.migrate()

        val yolo = xa.yolo
        import yolo._

        val user =
          User(username = "joebloggs", firstName = "Joe", lastName = "Bloggs")

        sql"insert into users (username, first_name, last_name) values (${user.username}, ${user.firstName}, ${user.lastName})".update.quick.unsafeRunSync

        whenReady(repository.getAllUsers.unsafeRunAsyncFuture) { users =>
          users should contain only user
        }

      }
    }
  }
}
