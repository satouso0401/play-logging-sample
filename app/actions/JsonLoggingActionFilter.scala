package actions

import com.google.inject.Inject
import play.api.Logger
import play.api.libs.json.JsObject
import play.api.mvc.{ActionFilter, Request}

import scala.concurrent.{ExecutionContext, Future}

class JsonLoggingActionFilter @Inject()(implicit ec: ExecutionContext) extends ActionFilter[Request] {

  val logger: Logger = Logger(this.getClass)

  def executionContext = ec
  def filter[A](input: Request[A]) = Future.successful {
    input.body match {
      case x: JsObject => logger.info(s"path: ${input.path}, body: ${x.toString()}")
      case _           =>
    }
    None
  }
}
