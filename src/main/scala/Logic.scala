import zio.*
import zio.json.{DeriveJsonCodec, JsonCodec}

case class TodoDTO(id: Option[Long], title: String, isDone: Boolean)

object TodoDTO:
  implicit val codec: JsonCodec[TodoDTO] = DeriveJsonCodec.gen[TodoDTO]

  def fromLogic(todo: Todo) =
    TodoDTO(id = Some(todo.id), title = todo.title, isDone = todo.isDone)

enum TodoFilter:
  case All, Active, Completed

trait Logic:
  def createTodo(todo: TodoDTO): ZIO[Any, Throwable, TodoDTO]

  def toggleTodo(id: Long): ZIO[Any, Throwable, TodoDTO]

  def getAllTodos(filter: TodoFilter): ZIO[Any, Throwable, List[TodoDTO]]

object Logic:
  def createTodo(todo: TodoDTO) = ZIO.serviceWithZIO[Logic](_.createTodo(todo))

  def toggleTodo(id: Long) = ZIO.serviceWithZIO[Logic](_.toggleTodo(id))

  def getAllTodos(filter: TodoFilter) = ZIO.serviceWithZIO[Logic](_.getAllTodos(filter))

case class LiveLogic(database: Database) extends Logic:
  override def createTodo(todo: TodoDTO): ZIO[Any, Throwable, TodoDTO] =
    val newTodo = Todo(id = 0, title = todo.title, isDone = false)
    database.createTodo(newTodo).map(TodoDTO.fromLogic)

  override def toggleTodo(id: Long) =
    for {
      maybeTodo <- database.getTodoById(id)
      todo <- ZIO.getOrFailWith(new Exception("Todo not found"))(maybeTodo)
      newTodo = todo.copy(isDone = !todo.isDone)
      _ <- database.updateTodo(newTodo)
    } yield TodoDTO.fromLogic(newTodo)

  override def getAllTodos(filter: TodoFilter) =
    val result = filter match {
      case TodoFilter.All => database.getAllTodos()
      case TodoFilter.Active => database.getTodosByCompleteness(isDone = false)
      case TodoFilter.Completed => database.getTodosByCompleteness(isDone = true)
    }
    result.map(_.map(TodoDTO.fromLogic))

object LiveLogic:
  val layer: ZLayer[Database, Nothing, Logic] = ZLayer.fromZIO(
    for {
      database <- ZIO.service[Database]
    } yield LiveLogic(database)
  )


