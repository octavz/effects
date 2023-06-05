import zio.*
import zio.config.*
import zio.config.magnolia.*
import zio.config.typesafe.*

case class DatabaseConfig(host: String, port: Int, database: String, user: String, password: String )
case class AppConfig(database: DatabaseConfig)

object AppConfig:
  val config: Config[AppConfig] = deriveConfig[AppConfig]
