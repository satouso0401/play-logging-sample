package controllers

import actions.{JsonLoggingActionFilter, RawBufferLoggingActionFilter}
import javax.inject.{Inject, Singleton}
import models.SampleRequestBody
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import actions.BodyParserFuncs.RichBP

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class SampleController @Inject()(
    cc: ControllerComponents,
    jsonLogging: JsonLoggingActionFilter,
    rawBuffLogging: RawBufferLoggingActionFilter
) extends AbstractController(cc) {

  val logger: Logger = Logger(this.getClass)

  // curl -X POST -H "Content-Type: application/json" -d '{"key1":"val1", "key2":"val2"}' localhost:9000/filter-log
  def filterLog(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    InternalServerError(Json.toJson(Map("status" -> "Error")))
  }

  // curl -X POST -H "Content-Type: application/json" -d '{"key1":"val1", "key2":"val2"}' localhost:9000/essential-filter-log
  def essentialFilterLog(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(Json.toJson(Map("status" -> "OK")))
  }

  // curl -X POST -H "Content-Type: application/json" -d '{"key1":"val1", "key2":"val2"}' localhost:9000/action-filter-log
  def actionFilterLog() = (Action(parse.json) andThen jsonLogging) { implicit request: Request[JsValue] =>
    request.body.validate[SampleRequestBody] match {
      case _: JsSuccess[SampleRequestBody] => Ok(Json.toJson(Map("status"         -> "OK")))
      case _: JsError                      => BadRequest(Json.toJson(Map("status" -> "Error")))
    }
  }

  // curl -X POST -H "Content-Type: application/json" -d '{"key1":"val1", "key2":"val2"}' localhost:9000/action-filter-log2
  def actionFilterLog2() = (Action(parse.json[SampleRequestBody] and parse.raw) andThen rawBuffLogging) { implicit request =>
    Ok(Json.toJson(Map("status" -> s"OK ${request.body._1.key1}")))
  }



}
