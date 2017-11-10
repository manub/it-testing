package net.manub.ittesting

import com.typesafe.config.ConfigFactory
import fs2.{Stream, Task}
import org.flywaydb.core.Flyway
import org.http4s._
import org.http4s.dsl._
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.StreamApp

object Main extends StreamApp {

  val config = ConfigFactory.load()

  val helloWorldService = HttpService {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name!")
  }

  override def stream(args: List[String]): Stream[Task, Nothing] = {

    val flyway = new Flyway()
    flyway.setDataSource(config.getString("database.url"),
                         config.getString("database.username"),
                         config.getString("database.password"))
    flyway.migrate()

    BlazeBuilder
      .bindHttp(8080, "localhost")
      .mountService(helloWorldService)
      .serve

  }

}
