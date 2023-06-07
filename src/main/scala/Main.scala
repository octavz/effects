import zio.*
import zio.config.typesafe.TypesafeConfigProvider
import zio.http.*
import zio.json.*
import zio.logging.{LogFilter, LogFormat}
import zio.logging.backend.SLF4J
import zio.metrics.*
import zio.metrics.connectors.{MetricsConfig, prometheus}

object Main extends ZIOAppDefault:

  override val bootstrap: ZLayer[Any, Nothing, Unit] =
    Runtime.removeDefaultLoggers
      >>> Runtime.setConfigProvider(TypesafeConfigProvider.fromResourcePath())
      >>> SLF4J.slf4j

  def countAllRequests(method: String, handler: String) =
    Metric.counterInt("count_all_requests").fromConst(1)
      .tagged(
        MetricLabel("method", method),
        MetricLabel("handler", handler)
      )

  val app =
    Http.collectZIO[Request] {
      case Method.GET -> !! / "todos" =>
        Logic.getAllTodos(TodoFilter.All).map(r => Response.json(r.toJson)) @@ countAllRequests("GET", "/todos")
      case Method.GET -> !! / "todos" / "completed" =>
        Logic.getAllTodos(TodoFilter.Completed).map(r => Response.json(r.toJson)) @@ countAllRequests("GET", "/todos/completed")
      case Method.GET -> !! / "todos" / "active" =>
        Logic.getAllTodos(TodoFilter.Active).map(r => Response.json(r.toJson)) @@ countAllRequests("GET", "/todos/active")
      case req@Method.POST -> !! / "todos" =>
        val io = for {
          bodyText <- req.body.asString
          dto <- ZIO.fromEither(bodyText.fromJson[TodoDTO]).mapError(Exception(_))
          ret <- Logic.createTodo(dto)
        } yield Response.json(ret.toJson)
        io @@ countAllRequests("POST", "/todos")
      case Method.PATCH -> !! / "todos" / id / "toggle" =>
        val io = for {
          longId <- ZIO.attempt(id.toLong)
          ret <- Logic.toggleTodo(longId)
        } yield Response.json(ret.toJson)
        io @@ countAllRequests("PATCH", "/toggle")
      case Method.GET -> !! / "metrics" =>
        ZIO.serviceWithZIO[prometheus.PrometheusPublisher](_.get.map(Response.text))
    }

  private val metricsConfig = ZLayer.succeed(MetricsConfig(1.seconds))

  private val httpConfig: ZLayer[Any, Config.Error, Server.Config] = ZLayer.fromZIO {
    for {
      appConfig <- ZIO.config[AppConfig](AppConfig.config)
    } yield Server.Config.default.port(appConfig.port)
  }

  override def run: ZIO[ZIOAppArgs with Scope, Throwable, Unit] =
    val http =
      app.catchAllZIO { t =>
        for {
          fiberId <- ZIO.fiberId
          _ <- ZIO.logErrorCause(t.getMessage, Cause.fail(t, StackTrace.fromJava(fiberId, t.getStackTrace)))
          ret <- ZIO.fail(Response.json(t.getMessage).withStatus(Status.InternalServerError))
        } yield ret
      }
    Server.serve(http).provide(httpConfig, Server.live, LiveLogic.layer, LiveDatabase.layer, PostgresDataSource.layer,
      prometheus.publisherLayer,
      prometheus.prometheusLayer,
      metricsConfig
    )