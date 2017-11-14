package net.manub.ittesting

import com.typesafe.config.Config
import doobie.imports._
import cats._
import cats.data._
import cats.implicits._
import fs2.Task
import fs2.interop.cats._

class UsersRepository(override val config: Config)
    extends PostgresConfiguration {

  val xa = DriverManagerTransactor[Task](
    PostgresDriver,
    postgresUrl,
    postgresUsername,
    postgresPassword
  )

  val all: Task[List[User]] =
    sql"select username, first_name, last_name FROM users"
      .query[User]
      .list
      .transact(xa)

  def save(user: User): Task[Int] =
    sql"insert into users (username, first_name, last_name) values (${user.username}, ${user.firstName}, ${user.lastName})"
      .update
      .run
      .transact(xa)
}
