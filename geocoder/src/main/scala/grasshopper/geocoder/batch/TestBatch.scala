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

case class ParsedOutputAddress(input: String, parsed: ParsedInputAddress)
case class BatchInputParsedAddress(input: String, parsedInputAddress: ParsedInputAddress)
case class CensusBatchGeocodeResult(input: String, latitude: Double, longitude: Double)
case class AddressPointsBatchGeocodeResult(input: String, latitude: Double, longitude: Double)

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
      "1 Main St City ST 00001"
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

  val censusFlow: Flow[ParsedOutputAddress, CensusBatchGeocodeResult, Unit] = {
    Flow[ParsedOutputAddress]
      .mapAsync(4) { p =>
        CensusClient.geocode(p.parsed).map { x =>
          if (x.isRight) {
            val result = x.right.getOrElse(CensusResult.empty)
            val longitude = if (!result.features.isEmpty) {
              result.features.toList.head.geometry.centroid.x
            } else {
              0
            }
            val latitude = if (!result.features.isEmpty) {
              result.features.toList.head.geometry.centroid.y
            } else {
              0
            }
            CensusBatchGeocodeResult(p.input, latitude, longitude)
          } else {
            val result = CensusResult.error
            CensusBatchGeocodeResult(p.input, 0.0, 0.0)
          }
        }
      }
  }

  val addressPointsFlow: Flow[String, AddressPointsBatchGeocodeResult, Unit] = {
    Flow[String]
      .mapAsync(4) { a =>
        AddressPointsClient.geocode(a).map { x =>
          if (x.isRight) {
            val result = x.right.getOrElse(AddressPointsResult.empty)
            val longitude = if (!result.features.isEmpty) {
              result.features.toList.head.geometry.centroid.x
            } else {
              0
            }
            val latitude = if (!result.features.isEmpty) {
              result.features.toList.head.geometry.centroid.y
            } else {
              0
            }
            AddressPointsBatchGeocodeResult(a, latitude, longitude)
          } else {
            AddressPointsBatchGeocodeResult(a, 0.0, 0.0)
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

  val g = FlowGraph.closed() { implicit builder: FlowGraph.Builder[Unit] =>
    import FlowGraph.Implicits._

    val broadcast = builder.add(Broadcast[String](2))
    val zip = builder.add(Zip[CensusBatchGeocodeResult, AddressPointsBatchGeocodeResult]())

    source ~> broadcast ~> parseFlow ~> parsedInputAddressFlow ~> censusFlow ~> zip.in0
    broadcast ~> addressPointsFlow ~> zip.in1
    zip.out ~> Flow[(CensusBatchGeocodeResult, AddressPointsBatchGeocodeResult)].map {
      case (b1, b2) =>
        if (b2.latitude != 0 && b2.longitude != 0) {
          b2
        } else {
          b1
        }
      //(b1, b2)
    } ~> sink

  }

  g.run()

}
