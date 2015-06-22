package grasshopper.geocoder.batch

import akka.actor.ActorSystem
import akka.stream.Supervision.Decider
import akka.stream.scaladsl._
import akka.stream.{ ActorFlowMaterializer, ActorFlowMaterializerSettings, Supervision }
import grasshopper.client.addresspoints.AddressPointsClient
import grasshopper.client.addresspoints.model.AddressPointsResult
import grasshopper.client.census.CensusClient
import grasshopper.client.census.model.{ CensusResult, ParsedInputAddress }
import grasshopper.client.parser.AddressParserClient
import grasshopper.client.parser.model.ParsedAddress

import scala.concurrent.ExecutionContext

case class BatchInputParsedAddress(input: String, parsedInputAddress: ParsedInputAddress)
case class BatchGeocodeResult(service: String, input: String, latitude: Double, longitude: Double) {
  //override def toString = s"${input},${latitude},${longitude}"
}

object TestBatch extends App {

  // Stream supervision, what to do with the stream when an element fails
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
      "1311 30th St NW Washington DC 20007",
      "3146 M St NW Washington DC 20007",
      "198 President St Arkansas City AR 71630",
      ""
    ).toIterator

  val source: Source[String, Unit] = Source(() => addresses)

  val parseFlow: Flow[String, ParsedAddress, Unit] = {
    Flow[String]
      .mapAsync(4)(a => AddressParserClient.standardize(a))
      .map { x =>
        if (x.isRight) {
          x.right.getOrElse(ParsedAddress.empty)
        } else {
          ParsedAddress.empty
        }
      }
  }

  case class ParsedOutputAddress(input: String, parsed: ParsedInputAddress)

  val parsedInputAddressFlow: Flow[ParsedAddress, ParsedOutputAddress, Unit] = {
    Flow[ParsedAddress]
      .map(a =>
        ParsedOutputAddress(
          a.input,
          ParsedInputAddress(
            a.parts.addressNumber.toInt,
            a.parts.streetName,
            a.parts.zip.toInt,
            a.parts.state
          )
        ))
  }

  val censusFlow: Flow[ParsedOutputAddress, BatchGeocodeResult, Unit] = {
    Flow[ParsedOutputAddress]
      .mapAsync(4) { p =>
        CensusClient.geocode(p.parsed).map { x =>
          if (x.isRight) {
            val result = x.right.getOrElse(CensusResult.empty)
            val longitude = result.features.toList.head.geometry.centroid.x
            val latitude = result.features.toList.head.geometry.centroid.y
            BatchGeocodeResult("census", p.input, latitude, longitude)
          } else {
            val result = CensusResult.error
            BatchGeocodeResult("census", p.input, 0.0, 0.0)
          }
        }
      }
  }

  val addressPointsFlow: Flow[String, BatchGeocodeResult, Unit] = {
    Flow[String]
      .mapAsync(4) { a =>
        AddressPointsClient.geocode(a).map { x =>
          if (x.isRight) {
            val result = x.right.getOrElse(AddressPointsResult.empty)
            val longitude = result.features.toList.head.geometry.centroid.x
            val latitude = result.features.toList.head.geometry.centroid.y
            BatchGeocodeResult("addresspoints", a, latitude, longitude)
          } else {
            val result = AddressPointsResult.error
            BatchGeocodeResult("addresspoints", a, 0.0, 0.0)
          }
        }
      }
  }

  val sink = Sink.foreach(println)

  //  source
  //    .via(parseFlow)
  //    .via(parsedInputAddressFlow)
  //    .via(censusFlow)
  //    .to(sink).run()

  val sink1 = Sink.foreach(println)
  val sink2 = Sink.foreach(println)

  val g = FlowGraph.closed() { implicit builder: FlowGraph.Builder[Unit] =>
    import FlowGraph.Implicits._

    val broadcast = builder.add(Broadcast[String](2))
    val merge = builder.add(Merge[BatchGeocodeResult](2))

    source ~> broadcast ~> parseFlow ~> parsedInputAddressFlow ~> censusFlow ~> merge ~> sink
    broadcast ~> addressPointsFlow ~> merge

  }

  g.run()

}
