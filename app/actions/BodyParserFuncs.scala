package actions

import akka.stream.SinkShape
import akka.stream.scaladsl.{Broadcast, GraphDSL, Sink}
import akka.util.ByteString
import play.api.libs.streams.Accumulator
import play.api.mvc.BodyParser

import scala.concurrent.ExecutionContext

object BodyParserFuncs {

  def combine[A, B](bodyParser1: BodyParser[A], bodyParser2: BodyParser[B])(
    implicit ec: ExecutionContext
  ): BodyParser[(A, B)] = BodyParser { rh =>
    val sink1 = bodyParser1(rh).toSink
    val sink2 = bodyParser2(rh).toSink

    val sinkGraph = GraphDSL.create(sink1, sink2) {
      case (fera, ferb) =>
        for {
          era <- fera
          erb <- ferb
        } yield {
          for {
            a <- era.right
            b <- erb.right
          } yield (a, b)
        }
    } { implicit builder => (s1, s2) =>
      import GraphDSL.Implicits._
      val bcast = builder.add(Broadcast[ByteString](2, eagerCancel = false))
      bcast.out(0) ~> s1
      bcast.out(1) ~> s2
      SinkShape(bcast.in)
    }

    Accumulator(Sink.fromGraph(sinkGraph))
  }

  implicit class RichBP[A](bodyParser1: BodyParser[A]) {

    def and[B](bodyParser2: BodyParser[B])(
      implicit ec: ExecutionContext
    ): BodyParser[(A, B)] = combine(bodyParser1, bodyParser2)

  }

}
