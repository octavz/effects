import com.zaxxer.hikari.*
import zio.*
import io.getquill.*
import io.getquill.jdbczio.Quill

import javax.sql.DataSource

case class User(email: String, password: String)

trait Database:
  def createUser(email: String, password: String): ZIO[Any, Throwable, Unit]

  def getUserByEmail(email: String): ZIO[Any, Throwable, User]

object PostgresContext extends PostgresZioJdbcContext(SnakeCase)

case class LiveDatabase(dataSource: DataSource) extends Database:

  import PostgresContext.*

  inline def users = quote {
    querySchema[User](
      "users",
      _.email -> "email",
      _.password -> "password",
    )
  }

  override def createUser(email: String, password: String): ZIO[Any, Throwable, Unit] =
    inline def q = quote {
      users.insertValue(lift(User(email = email, password = password)))
    }
    run(q)
      .provideEnvironment(ZEnvironment(dataSource))
      .unit

  override def getUserByEmail(email: String): ZIO[Any, Throwable, User] = ???

object PostgresDataSource:
  val layer: ZLayer[Any, Throwable, DataSource] = ZLayer.fromZIO(
    for {
      appConfig <- ZIO.config[AppConfig](AppConfig.config)
      databaseConfig = appConfig.database
      hikariConfig <- ZIO.attempt {
        val c = HikariConfig()
        c.setSchema(databaseConfig.schema)
        c.setDriverClassName("org.postgresql.Driver")
        c.setJdbcUrl(databaseConfig.url)
        c.setUsername(databaseConfig.user)
        c.setPassword(databaseConfig.password)
        c.setMaximumPoolSize(databaseConfig.poolSize)
        c
      }
      dataSource <- ZIO.attempt(HikariDataSource(hikariConfig))
    } yield  dataSource)

object Database:
  val layer: ZLayer[DataSource, Throwable, Database] = ZLayer.fromZIO(
    for {
      dataSource <- ZIO.service[DataSource]
    } yield LiveDatabase(dataSource)
  )
