import zio.*
import zio.Console.printLine
import zio.config.typesafe.TypesafeConfigProvider
import zio.http.*

object Main extends ZIOAppDefault:

  override val bootstrap: ZLayer[Any, Nothing, Unit] =
    Runtime.setConfigProvider(
      TypesafeConfigProvider
        .fromResourcePath()
    )

  val app =
    Http.collect[Request] {
      case Method.GET -> root / "text" => Response..text("Hello World!")
    }
  override def run: ZIO[ZIOAppArgs with Scope, Throwable , Unit] =
    for {
      _ <- Server.serve(app).provide(Server.default)
      //ZIO.serviceWithZIO[Database] (service => service.createUser("john@example.com", "qwertasdf")).provide(Database.layer, PostgresDataSource.layer)
    } yield ()