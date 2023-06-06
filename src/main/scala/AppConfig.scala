import zio.*
import zio.config.*
import zio.config.magnolia.*
import zio.config.typesafe.*

case class DatabaseConfig(host: String, port: Int, database: String, schema: String, user: String, password: String, poolSize: Int ):
  val url = s"jdbc:postgresql://$host:$port/$database"

case class AppConfig(database: DatabaseConfig)

object AppConfig:
  val config: Config[AppConfig] = deriveConfig[AppConfig]
