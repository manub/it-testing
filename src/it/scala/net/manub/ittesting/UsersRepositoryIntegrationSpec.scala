package net.manub.ittesting

import com.typesafe.config.ConfigFactory
import com.whisk.docker.impl.spotify.DockerKitSpotify
import doobie.imports._
import fs2.Task
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers, WordSpec}
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
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with DockerTestKit // scalatest integration
    with DockerKitSpotify // docker client implementation
    with DockerPostgresService { // container rules

  override val config = ConfigFactory.load()

  val transactor = DriverManagerTransactor[Task](
    PostgresDriver,
    postgresUrl,
    postgresUsername,
    postgresPassword
  )

  val repository = new UsersRepository(config)

  override def beforeAll(): Unit = {
    super.beforeAll()
    // if you want to write more integration test suites and run the migration only once, a different approach is needed
    val flyway = new Flyway()
    flyway.setDataSource(postgresUrl, postgresUsername, postgresPassword)
    flyway.migrate()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    sql"truncate table users".update.run.transact(transactor).unsafeRunSync
  }

  "users repository" when {

    val user =
      User(username = "joebloggs", firstName = "Joe", lastName = "Bloggs")

    "getting all the users" should {
      "return all users on the database" in {

        val insertAUser =
          sql"insert into users (username, first_name, last_name) values (${user.username}, ${user.firstName}, ${user.lastName})".update.run
            .transact(transactor)
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

        whenReady(repository.save(user).unsafeRunAsyncFuture()) {
          numberOfRows =>
            numberOfRows shouldBe 1

            sql"select username, first_name, last_name FROM users"
              .query[User]
              .list
              .transact(transactor)
              .unsafeRunAsyncFuture()
              .futureValue should contain only user
        }
      }
    }
  }
}
