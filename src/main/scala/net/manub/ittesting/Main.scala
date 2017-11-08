package net.manub.ittesting

import fs2.{Stream, Task}
import org.http4s._
import org.http4s.dsl._
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.StreamApp

object Main extends StreamApp {

  val helloWorldService = HttpService {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name!")
  }

  override def stream(args: List[String]): Stream[Task, Nothing] =
    BlazeBuilder.bindHttp(8080, "localhost").mountService(helloWorldService).serve

}
