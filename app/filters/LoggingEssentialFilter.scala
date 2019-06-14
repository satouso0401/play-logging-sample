package filters

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.FlowShape
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Sink}
import akka.util.ByteString
import com.google.inject.Inject
import play.api.libs.streams.Accumulator
import play.api.mvc.{EssentialAction, EssentialFilter, RequestHeader, Result}

import scala.concurrent.ExecutionContext

class LoggingEssentialFilter @Inject()(actorSystem: ActorSystem)(implicit ec: ExecutionContext) extends EssentialFilter {

  private val logger           = org.slf4j.LoggerFactory.getLogger(this.getClass)
  private implicit val logging = Logging(actorSystem.eventStream, logger.getName)

  override def apply(next: EssentialAction): EssentialAction = new EssentialAction {
    override def apply(request: RequestHeader): Accumulator[ByteString, Result] = {
      val accumulator: Accumulator[ByteString, Result] = next(request)

      val flow: Flow[ByteString, ByteString, NotUsed] = Flow[ByteString].log("byteflow")
      val sink =
        Sink.foreach[ByteString](x =>
          if (request.path == "/essential-filter-log") {
            logger.info(s"method:${request.method}, url:${request.uri}, body:${x.utf8String}")
        })

      def logGraph() = {
        Flow.fromGraph(
          GraphDSL.create() { implicit builder =>
            import GraphDSL.Implicits._
            val bcast = builder.add(Broadcast[ByteString](2, eagerCancel = false))
            bcast.out(1) ~> flow ~> sink
            FlowShape(bcast.in, bcast.out(0))
          }
        )
      }

      val accumulatorWithResult: Accumulator[ByteString, Result] =
        accumulator.through(logGraph()).map { result =>
          logger.info(s"method:${request.method}, url:${request.uri}, status:${result.header.status}")
          result
        }

      accumulatorWithResult
    }
  }
}
