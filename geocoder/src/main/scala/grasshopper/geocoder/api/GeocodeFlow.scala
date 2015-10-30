package grasshopper.geocoder.api

import akka.stream.scaladsl._
import feature.Feature
import geometry.Point
import grasshopper.client.addresspoints.AddressPointsClient
import grasshopper.client.census.CensusClient
import grasshopper.client.parser.AddressParserClient
import grasshopper.client.parser.model.ParsedAddress
import grasshopper.geocoder.model._
import grasshopper.geocoder.search.addresspoints.AddressPointsGeocode
import grasshopper.geocoder.search.census.CensusGeocode
import grasshopper.model.addresspoints.AddressPointsResult
import grasshopper.model.census.{ ParsedInputAddress, CensusResult }
import org.elasticsearch.client.Client

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

trait GeocodeFlow extends AddressPointsGeocode with CensusGeocode {

  def client: Client

  def parsedInputBatchAddressFlow: Flow[ParsedAddress, ParsedOutputBatchAddress, Unit] = {
    Flow[ParsedAddress]
      .map(a =>
        ParsedOutputBatchAddress(
          a.input,
          ParsedInputAddress(
            a.parts.addressNumber,
            a.parts.streetName,
            a.parts.city,
            a.parts.zip,
            a.parts.state
          )
        ))
  }

  def censusFlow(implicit ec: ExecutionContext): Flow[ParsedOutputBatchAddress, CensusGeocodeBatchResult, Unit] = {
    Flow[ParsedOutputBatchAddress]
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
            CensusGeocodeBatchResult(p.input, latitude, longitude)
          } else {
            val result = CensusResult.error
            CensusGeocodeBatchResult(p.input, 0.0, 0.0)
          }
        }
      }
  }

  def addressPointsFlow(implicit ec: ExecutionContext): Flow[String, AddressPointsGeocodeBatchResult, Unit] = {
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
            AddressPointsGeocodeBatchResult(a, latitude, longitude)
          } else {
            AddressPointsGeocodeBatchResult(a, 0.0, 0.0)
          }
        }
      }
  }

  def chooseGeocode: Flow[(CensusGeocodeBatchResult, AddressPointsGeocodeBatchResult), BatchGeocodeResult, Unit] = {
    Flow[(CensusGeocodeBatchResult, AddressPointsGeocodeBatchResult)].map {
      case (b1, b2) =>
        if (b2.latitude != 0 && b2.longitude != 0) {
          b2
        } else {
          b1
        }
    }
  }

  //def geocodeFlow(implicit ec: ExecutionContext): Flow[String, BatchGeocodeResult, Unit] = {
  //  Flow() { implicit b =>
  //    import FlowGraph.Implicits._

  //    val input = b.add(Flow[String])
  //    val broadcast = b.add(Broadcast[String](2))
  //    val parse = b.add(parseFlow)
  //    val parseInput = b.add(parsedInputAddressFlow)
  //    val census = b.add(censusFlow)
  //    val addressPoints = b.add(addressPointsFlow)
  //    val zip = b.add(Zip[CensusGeocodeBatchResult, AddressPointsGeocodeBatchResult]())
  //    val choose = b.add(chooseGeocode)

  //    input ~> broadcast ~> parse ~> parseInput ~> census ~> zip.in0
  //    broadcast ~> addressPoints ~> zip.in1
  //    zip.out ~> choose

  //    (input.inlet, choose.outlet)
  //  }
  //}

  // New definitions for geocoding from here

  def parseFlow: Flow[String, ParsedAddress, Unit] = {
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

  def parsedInputAddressFlow: Flow[ParsedAddress, ParsedInputAddress, Unit] = {
    Flow[ParsedAddress]
      .map(a =>
        ParsedInputAddress(
          a.parts.addressNumber,
          a.parts.streetName,
          a.parts.city,
          a.parts.zip,
          a.parts.state
        ))
  }

  def geocodePointFlow: Flow[String, Feature, Unit] = {
    Flow[String]
      .map(s => geocodePoint1(client, "address", "point", s, 1).head)
  }

  def geocodeLineFlow: Flow[ParsedInputAddress, Feature, Unit] = {
    Flow[ParsedInputAddress]
      .map(p => geocodeLine1(client, "census", "addrfeat", p, 1).head)
  }

  def tupleToListFlow[T]: Flow[(T, T), List[T], Unit] = {
    Flow[(T, T)]
      .map(t => List(t._1, t._2))
  }

  def generateResponseFlow: Flow[(ParsedAddress, List[Feature]), GeocodeResponse, Unit] = {
    Flow[(ParsedAddress, List[Feature])]
      .map(t => GeocodeResponse(t._1, t._2))
  }

  def filterFeatureListFlow: Flow[List[Feature], List[Feature], Unit] = {
    Flow[List[Feature]].map(xs => xs.filter(f => f.geometry.centroid.x != 0 && f.geometry.centroid.y != 0))
  }

  def geocodeSingleFlow(implicit ec: ExecutionContext): Flow[String, GeocodeResponse, Unit] = {
    Flow() { implicit b =>
      import FlowGraph.Implicits._

      val input = b.add(Flow[String])
      val broadcastParsed = b.add(Broadcast[ParsedAddress](2))
      val broadcastInput = b.add(Broadcast[String](2))
      val pFlow = b.add(parseFlow)
      val pInputFlow = b.add(parsedInputAddressFlow)
      val point = b.add(geocodePointFlow)
      val line = b.add(geocodeLineFlow)
      val zip = b.add(Zip[Feature, Feature])
      val features = b.add(tupleToListFlow[Feature].via(filterFeatureListFlow))
      val zip1 = b.add(Zip[ParsedAddress, List[Feature]])
      val response = b.add(generateResponseFlow)

      //val resp = b.add(Flow[GeocodeResponse])

      input ~> broadcastInput ~> pFlow ~> broadcastParsed ~> pInputFlow ~> line ~> zip.in0
      broadcastParsed ~> zip1.in0
      broadcastInput ~> point ~> zip.in1
      zip.out ~> features ~> zip1.in1
      zip1.out ~> response

      (input.inlet, response.outlet)

    }
  }
}

