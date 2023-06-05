import zio.*

case class User(email: String, password: String)

trait Database:
  def createUser(email: String, password: String): ZIO[Any, Exception, Unit]
  def getUserByEmail(email: String): ZIO[Any, Exception, User]

case class LiveDatabase(appConfig: AppConfig) extends Database:
  override def createUser(email: String, password: String): ZIO[Any, Exception, Unit] =
    ZIO.logInfo(appConfig.toString)

  override def getUserByEmail(email: String): ZIO[Any, Exception, User] = ???

object Database:
  val layer: ZLayer[Any, Exception, Database] = ZLayer.fromZIO(
    for {
      appConfig <- ZIO.config[AppConfig](AppConfig.config)
    } yield LiveDatabase(appConfig)

  )
