package net.manub.ittesting

import fs2.{Stream, Task}
import org.flywaydb.core.Flyway
import org.http4s._
import org.http4s.dsl._
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.StreamApp

object Main extends StreamApp {

  val helloWorldService = HttpService {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name!")
  }

  override def stream(args: List[String]): Stream[Task, Nothing] = {

    val flyway = new Flyway()
    flyway.setDataSource("jdbc:postgres:")


    BlazeBuilder.bindHttp(8080, "localhost").mountService(helloWorldService).serve

  }

}
