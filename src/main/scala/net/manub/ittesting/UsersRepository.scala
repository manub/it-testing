package net.manub.ittesting

import com.typesafe.config.Config
import doobie.imports._
import cats._
import cats.data._
import cats.implicits._
import fs2.Task
import fs2.interop.cats._

class UsersRepository(config: Config) {

  val url = config.getString("database.url")
  val username = config.getString("database.username")
  val password = config.getString("database.password")

  val xa = DriverManagerTransactor[Task](
    "org.postgresql.Driver",
    url,
    username,
    password
  )

  val getAllUsers: Task[List[User]] =
    sql"select username, first_name, last_name FROM users"
      .query[User]
      .list
      .transact(xa)
}
