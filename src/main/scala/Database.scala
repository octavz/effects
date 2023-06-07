import com.zaxxer.hikari.*
import zio.*
import io.getquill.*
import io.getquill.jdbczio.Quill

import javax.sql.DataSource

case class Todo(id: Long, title: String, isDone: Boolean)

trait Database:
  def createTodo(todo: Todo): ZIO[Any, Throwable, Todo]

  def updateTodo(todo: Todo): ZIO[Any, Throwable, Todo]

  def getTodoById(id: Long): ZIO[Any, Throwable, Option[Todo]]

  def getAllTodos(): ZIO[Any, Throwable, List[Todo]]

  def getTodosByCompleteness(isDone: Boolean): ZIO[Any, Throwable, List[Todo]]

object PostgresContext extends PostgresZioJdbcContext(SnakeCase)

case class LiveDatabase(dataSource: DataSource) extends Database:

  import PostgresContext.*

  override def createTodo(todo: Todo): ZIO[Any, Throwable, Todo] =
    inline def q = quote {
      query[Todo].insertValue(lift(todo)).returningGenerated(_.id)
    }

    run(q)
      .map(id => todo.copy(id = id))
      .provideEnvironment(ZEnvironment(dataSource))


  override def updateTodo(todo: Todo): ZIO[Any, Throwable, Todo] =
    val q = quote {
      query[Todo].filter(_.id == lift(todo.id)).updateValue(lift(todo))
    }
    run(q)
      .provideEnvironment(ZEnvironment(dataSource))
      .as(todo)

  override def getTodoById(id: Long): ZIO[Any, Throwable, Option[Todo]] =
    val q = quote {
      query[Todo].filter(_.id == lift(id))
    }
    val io = for {
      str <- PostgresContext.translate(q)
      _ <- ZIO.debug(str)
      r <- run(q).map(_.headOption)
    } yield r
    io.provideEnvironment(ZEnvironment(dataSource))

  override def getAllTodos(): ZIO[Any, Throwable, List[Todo]] =
    val q = quote {
      query[Todo]
    }
    run(q)
      .provideEnvironment(ZEnvironment(dataSource))

  override def getTodosByCompleteness(isDone: Boolean): ZIO[Any, Throwable, List[Todo]] =
    val q = quote {
      query[Todo].filter(_.isDone == lift(isDone))
    }
    run(q)
      .provideEnvironment(ZEnvironment(dataSource))

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
    } yield dataSource)

object LiveDatabase:
  val layer: ZLayer[DataSource, Throwable, Database] = ZLayer.fromZIO(
    for {
      dataSource <- ZIO.service[DataSource]
    } yield LiveDatabase(dataSource)
  )
