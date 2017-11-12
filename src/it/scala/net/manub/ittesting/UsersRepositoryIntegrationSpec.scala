package net.manub.ittesting

import com.typesafe.config.ConfigFactory
import com.whisk.docker.impl.spotify.DockerKitSpotify
import doobie.imports._
import fs2.Task
import org.scalatest.{Matchers, WordSpec}
import com.whisk.docker.scalatest.DockerTestKit
import fs2.interop.cats._
import org.flywaydb.core.Flyway
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future

class UsersRepositoryIntegrationSpec
    extends WordSpec
    with Matchers
    with ScalaFutures
    with PostgresConfiguration
    with DockerTestKit // scalatest integration
    with DockerKitSpotify // docker client implementation
    with DockerPostgresService { // container rules

  override val config = ConfigFactory.load()

  val xa = DriverManagerTransactor[Task](
    PostgresDriver,
    postgresUrl,
    postgresUsername,
    postgresPassword
  )

  val repository = new UsersRepository(config)

  "users repository" when {

    val user =
      User(username = "joebloggs", firstName = "Joe", lastName = "Bloggs")

    "getting all the users" should {
      "return all users on the database" in {

        val flyway = new Flyway()
        flyway.setDataSource(postgresUrl, postgresUsername, postgresPassword)
        flyway.migrate()

        val insertAUser =
          sql"insert into users (username, first_name, last_name) values (${user.username}, ${user.firstName}, ${user.lastName})".update.run
            .transact(xa)
            .unsafeRunAsyncFuture()

        val users: Future[List[User]] = for {
          _ <- insertAUser
          users <- repository.all.unsafeRunAsyncFuture()
        } yield users

        users.futureValue should contain only user
      }
    }

    "saving a user" should {
      "persist it into the database" in {

        //TODO: perform database cleanup and migrations properly!
        sql"truncate table users".update.run.transact(xa).unsafeRunSync

//        val flyway = new Flyway()
//        flyway.setDataSource(postgresUrl, postgresUsername, postgresPassword)
//        flyway.migrate()
//

        whenReady(repository.save(user).unsafeRunAsyncFuture()) {
          numberOfRows =>
            numberOfRows shouldBe 1

            sql"select username, first_name, last_name FROM users"
              .query[User]
              .list
              .transact(xa)
              .unsafeRunAsyncFuture()
              .futureValue should contain only user
        }
      }
    }
  }
}
