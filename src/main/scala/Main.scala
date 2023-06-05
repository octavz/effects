import zio.*
import zio.Console.printLine

object Main extends ZIOAppDefault:
  
  override def run: ZIO[Environment & ZIOAppArgs & Scope, Exception, Unit] =
    printLine("Welcome to your first ZIO app!")