package actions

import com.google.inject.Inject
import play.api.Logger
import play.api.mvc.{ActionFilter, RawBuffer, Request}

import scala.concurrent.{ExecutionContext, Future}

class RawBufferLoggingActionFilter @Inject()(implicit ec: ExecutionContext) extends ActionFilter[Request] {

  val logger: Logger = Logger(this.getClass)

  def executionContext = ec
  def filter[A](input: Request[A]) = Future.successful {
    input.body match {
      case (_, x: RawBuffer) =>
        logger.info(s"path: ${input.path}, body: ${x.asBytes().get.utf8String}")
      case _ =>
    }
    None
  }
}
