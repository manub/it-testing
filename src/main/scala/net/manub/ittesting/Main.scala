package net.manub.ittesting

import com.typesafe.config.ConfigFactory
import fs2.{Stream, Task}
import io.circe.generic.auto._
import io.circe.syntax._
import org.flywaydb.core.Flyway
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.StreamApp

object Main extends StreamApp {

  val config = ConfigFactory.load()
  val usersRepository = new UsersRepository(config)

  val helloWorldService = HttpService {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name!")
  }

  val usersService = HttpService {
    case GET -> Root / "users" =>
      Ok(usersRepository.getAllUsers.map(_.asJson))
  }

  override def stream(args: List[String]): Stream[Task, Nothing] = {

    migrateDatabase()

    BlazeBuilder
      .bindHttp(8080, "localhost")
      .mountService(helloWorldService)
      .mountService(usersService)
      .serve

  }

  // non-pure!
  private def migrateDatabase() = {
    val flyway = new Flyway()
    flyway.setDataSource(config.getString("database.url"),
      config.getString("database.username"),
      config.getString("database.password"))
    flyway.migrate()
  }
}
