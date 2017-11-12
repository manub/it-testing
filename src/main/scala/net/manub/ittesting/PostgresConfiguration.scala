package net.manub.ittesting

import com.typesafe.config.Config

trait PostgresConfiguration {

  def config: Config

  lazy val postgresUsername: String = config.getString("postgres.username")
  lazy val postgresPassword: String = config.getString("postgres.password")
  lazy val postgresDatabase: String = config.getString("postgres.database")
  lazy val postgresHost: String = config.getString("postgres.host")
  lazy val postgresPort: String = config.getString("postgres.port")
  lazy val postgresUrl: String =
    s"jdbc:postgresql://$postgresHost:$postgresPort/$postgresDatabase"

  val PostgresDriver: String = "org.postgresql.Driver"
}
