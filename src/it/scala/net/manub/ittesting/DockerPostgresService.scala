package net.manub.ittesting

import java.sql.DriverManager

import com.typesafe.config.Config
import com.whisk.docker.{
  DockerCommandExecutor,
  DockerContainer,
  DockerContainerState,
  DockerKit,
  DockerReadyChecker
}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait DockerPostgresService extends DockerKit {

  def config: Config

  import scala.concurrent.duration._

  val PostgresAdvertisedPort = 5432
  val PostgresExposedPort = 5432

  lazy val postgresUrl = config.getString("database.url")
  lazy val postgresUser = config.getString("database.username")
  lazy val postgresPassword = config.getString("database.password")
  lazy val postgresContainer = DockerContainer("postgres:latest")
    .withPorts((PostgresAdvertisedPort, Some(PostgresExposedPort)))
    .withEnv(s"POSTGRES_USER=$postgresUser",
             s"POSTGRES_PASSWORD=$postgresPassword",
             s"POSTGRES_DB=mydb") // TODO change to url
    .withReadyChecker(
      new PostgresReadyChecker(postgresUrl,
                               postgresUser,
                               postgresPassword,
                               Some(PostgresExposedPort))
        .looped(15, 1.second)
    )

  abstract override def dockerContainers: List[DockerContainer] =
    postgresContainer :: super.dockerContainers
}

class PostgresReadyChecker(url: String,
                           user: String,
                           password: String,
                           port: Option[Int] = None)
    extends DockerReadyChecker {

  override def apply(container: DockerContainerState)(
      implicit docker: DockerCommandExecutor,
      ec: ExecutionContext): Future[Boolean] =
    container
      .getPorts()
      .map(ports =>
        Try {
          Class.forName("org.postgresql.Driver")
//          val url =
//            s"jdbc:postgresql://${docker.host}:${port.getOrElse(ports.values.head)}/"
          DriverManager.getConnection(url, user, password).close()
          true
        }.getOrElse(false))
}
