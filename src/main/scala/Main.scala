import zio.*
import zio.Console.printLine
import zio.config.typesafe.TypesafeConfigProvider


object Main extends ZIOAppDefault:

  override val bootstrap: ZLayer[Any, Nothing, Unit] =
    Runtime.setConfigProvider(
      TypesafeConfigProvider
        .fromResourcePath()
    )

  override def run: ZIO[ZIOAppArgs with Scope, Exception , Unit] =
    ZIO.serviceWithZIO[Database](service => service.createUser("", ""))
      .provide(Database.layer)