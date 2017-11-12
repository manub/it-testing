package net.manub.ittesting

import java.sql.DriverManager

import com.whisk.docker.{
  DockerCommandExecutor,
  DockerContainer,
  DockerContainerState,
  DockerKit,
  DockerReadyChecker
}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait DockerPostgresService extends DockerKit with PostgresConfiguration {

  import scala.concurrent.duration._

  val PostgresAdvertisedPort = 5432

  lazy val postgresContainer: DockerContainer =
    DockerContainer("postgres:latest")
      .withPorts((PostgresAdvertisedPort, Some(postgresPort)))
      .withEnv(s"POSTGRES_USER=$postgresUsername",
               s"POSTGRES_PASSWORD=$postgresPassword",
               s"POSTGRES_DB=$postgresDatabase")
      .withReadyChecker(
        new PostgresReadyChecker(postgresUrl,
                                 postgresUsername,
                                 postgresPassword).looped(15, 1.second)
      )

  abstract override def dockerContainers: List[DockerContainer] =
    postgresContainer :: super.dockerContainers

}

class PostgresReadyChecker(url: String, username: String, password: String)
    extends DockerReadyChecker {

  override def apply(container: DockerContainerState)(
      implicit docker: DockerCommandExecutor,
      ec: ExecutionContext): Future[Boolean] =
    container
      .getPorts()
      .map(_ =>
        Try {
          Class.forName("org.postgresql.Driver")
          DriverManager
            .getConnection(url, username, password)
            .close()
          true
        }.getOrElse(false))
}
