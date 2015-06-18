package grasshopper.geocoder.batch

import akka.actor.ActorSystem
import akka.stream.Supervision.Decider
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorFlowMaterializer, ActorFlowMaterializerSettings, Supervision}
import grasshopper.client.addresspoints.AddressPointsClient
import grasshopper.client.census.model.ParsedInputAddress
import grasshopper.client.parser.AddressParserClient
import grasshopper.client.parser.model.ParsedAddress

import scala.concurrent.{ExecutionContext, Future}

object TestBatch extends App {

  // Stream supervision, what to do when an element fails
  val decider: Decider = exc => exc match {
    case _: ArithmeticException => Supervision.Stop
    case _ => Supervision.Resume
  }

  implicit val actorSystem = ActorSystem("sys")
  implicit val mat = ActorFlowMaterializer(
    ActorFlowMaterializerSettings(actorSystem)
      .withSupervisionStrategy(decider)
  )
  implicit val ec: ExecutionContext = actorSystem.dispatcher


  val addresses =
    List(
      ParsedInputAddress(1311, "30th St", 20007, "DC"),
      ParsedInputAddress(2100, "31th St", 20007, "DC"),
      ParsedInputAddress.empty
    ).toIterator

  def futureAddress(address: ParsedInputAddress): Future[ParsedInputAddress] = {
    Future(address)
  }

  val source: Source[ParsedInputAddress,  Unit] = Source(() => addresses)

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
      .mapAsync(2)(s => AddressPointsClient.geocode(s))
      .map { x =>
        if (x.isRight) x.right.get.features.head
      }
  }

  val sink = Sink.foreach(println)

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
