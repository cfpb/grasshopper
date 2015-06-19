package grasshopper.geocoder.batch

import akka.actor.ActorSystem
import akka.stream.Supervision.Decider
import akka.stream.scaladsl.{ Flow, Sink, Source, FlowGraph }
import akka.stream.{ ActorFlowMaterializer, ActorFlowMaterializerSettings, Supervision }
import grasshopper.client.addresspoints.AddressPointsClient
import grasshopper.client.census.model.ParsedInputAddress
import grasshopper.client.parser.AddressParserClient
import grasshopper.client.parser.model.ParsedAddress
import scala.concurrent.{ ExecutionContext, Future }
import akka.stream.impl.Broadcast
import akka.stream.scaladsl.Merge
import akka.stream.scaladsl.Broadcast

object TestBatch extends App {

  // Stream supervision, what to do when an element fails
  val decider: Decider = exc => exc match {
    case _: ArithmeticException => Supervision.Stop
    case _ => Supervision.Resume
  }

  implicit val actorSystem = ActorSystem("sys")
  implicit val mat = ActorFlowMaterializer(
    ActorFlowMaterializerSettings(actorSystem)
      .withSupervisionStrategy(decider))
  implicit val ec: ExecutionContext = actorSystem.dispatcher

  val addresses =
    List(
      ParsedInputAddress(1311, "30th St", 20007, "DC"),
      ParsedInputAddress(2100, "31th St", 20007, "DC"),
      ParsedInputAddress.empty).toIterator

  def futureAddress(address: ParsedInputAddress): Future[ParsedInputAddress] = {
    Future(address)
  }

  val source: Source[ParsedInputAddress, Unit] = Source(() => addresses)

  val parseFlow = {
    Flow[ParsedInputAddress]
      .mapAsync(2)(a => AddressParserClient.standardize(a.toString().replace(" ", "+")))
      .map { x =>
        if (x.isRight) {
          x.right.getOrElse(ParsedAddress.empty).input.toString
        } else if (x.isLeft) {
          "Address cannot be parsed"
        }
      }
  }

  val geocodeFlow = {
    Flow[String]
      .mapAsync(2)(s => AddressPointsClient.geocode(s.replace(" ", "+")))
      .map { x =>
        if (x.isRight) x.right.get.features.head
      }
  }

  //  def graphFlow = {
  //    Flow() { implicit builder: FlowGraph.Builder[Unit] =>
  //      import FlowGraph.Implicits._
  //      val in = Source(1 to 10)
  //      val out = Sink.ignore
  //
  //      val bcast = builder.add(Broadcast[Int](2))
  //      val merge = builder.add(Merge[Int](2))
  //
  //      val f1, f2, f3, f4 = Flow[Int].map(_ + 10)
  //
  //      in ~> f1 ~> bcast ~> f2 ~> merge ~> f3 ~> out
  //      bcast ~> f4 ~> merge
  //
  //    }
  //  }

  // def graphFlow = {
  //   Flow() { implicit b =>
  //     import FlowGraph.Implicits._

  //     val out = Sink.ignore

  //     val parse = b.add(parseFlow)
  //     val geocode = b.add(geocodeFlow)

  //     source ~> parse ~> out

  //   }
  // }

  val sink = Sink.foreach(println)

  source.via(parseFlow).to(sink).run()

  //  source
  //    .via(parseFlow)
  //    .grouped(10000)
  //    .runWith(Sink.head)
  //    .map(e => println(e))

  //source.via(parseFlow).via(geocodeFlow).to(sink).run()

  // source ~> parseFlow ~> geocodeFlow ~> sink

  //  val g = FlowGraph {
  //
  //  }

  //source.runWith(sink)

}
