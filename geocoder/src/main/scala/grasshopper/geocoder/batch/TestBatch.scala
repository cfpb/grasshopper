package grasshopper.geocoder.batch

import akka.actor.ActorSystem
import akka.stream.Supervision.Decider
import akka.stream.scaladsl._
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings, Supervision }
import grasshopper.geocoder.api.GeocodeFlows
import scala.concurrent.ExecutionContext
import akka.util.ByteString
import akka.stream.io.Framing

object TestBatch extends App {

  // Stream supervision, what to do with the stream when an element fails
  val decider: Decider = exc => exc match {
    case _: ArithmeticException => Supervision.Stop
    case _ => Supervision.Resume
  }

  implicit val system = ActorSystem("sys")
  implicit val mat = ActorMaterializer(
    ActorMaterializerSettings(system)
      .withSupervisionStrategy(decider)
  )
  implicit val ec: ExecutionContext = system.dispatcher

  val addresses =
    List(
      "1311 30th St NW Washington DC 20007",
      "3146 M St NW Washington DC 20007",
      "198 President St Arkansas City AR 71630",
      "1 Main St City ST 00001"
    ).toIterator

  val addresses2 = List(
    ByteString("1311 30th St NW Washington DC 20007\r\n"),
    ByteString("3146 M St NW Washington DC 20007\r\n")).toIterator

  val source = Source(() => addresses2)

  val linesStream = source.via(
    Framing.delimiter(
      ByteString("\r\n"),
      maximumFrameLength = 100,
      allowTruncation = true))
    .map(_.utf8String)

  //linesStream.to(Sink.foreach { x => println(x) }).run()

  linesStream
    .map { x => println(x.toString()); x }
    .via(GeocodeFlows.geocode)
    .to(Sink.foreach(println)).run()

  //  val source2 = Source(() => addresses)
  //
  //  source2
  //    .via(GeocodeFlows.geocode)
  //    .to(Sink.foreach(println)).run()
}
