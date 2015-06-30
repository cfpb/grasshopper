package grasshopper.geocoder.api

import akka.stream.scaladsl._
import grasshopper.client.addresspoints.AddressPointsClient
import grasshopper.client.addresspoints.model.AddressPointsResult
import grasshopper.client.census.CensusClient
import grasshopper.client.census.model.{ CensusResult, ParsedInputAddress }
import grasshopper.client.parser.AddressParserClient
import grasshopper.client.parser.model.ParsedAddress
import grasshopper.geocoder.model.{ BatchGeocodeResult, AddressPointsGeocodeBatchResult, CensusGeocodeBatchResult, ParsedOutputBatchAddress }

import scala.concurrent.ExecutionContext

object GeocodeFlows {

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

  def parsedInputAddressFlow: Flow[ParsedAddress, ParsedOutputBatchAddress, Unit] = {
    Flow[ParsedAddress]
      .map(a =>
        ParsedOutputBatchAddress(
          a.input,
          ParsedInputAddress(
            a.parts.addressNumber.toInt,
            a.parts.streetName,
            a.parts.zip.toInt,
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

  def geocode(implicit ec: ExecutionContext): Flow[String, BatchGeocodeResult, Unit] = {
    Flow() { implicit b =>
      import FlowGraph.Implicits._

      val input = b.add(Flow[String])
      val broadcast = b.add(Broadcast[String](2))
      val parse = b.add(parseFlow)
      val parseInput = b.add(parsedInputAddressFlow)
      val census = b.add(censusFlow)
      val addressPoints = b.add(addressPointsFlow)
      val zip = b.add(Zip[CensusGeocodeBatchResult, AddressPointsGeocodeBatchResult]())
      val choose = b.add(chooseGeocode)

      input ~> broadcast ~> parse ~> parseInput ~> census ~> zip.in0
      broadcast ~> addressPoints ~> zip.in1
      zip.out ~> choose

      (input.inlet, choose.outlet)
    }
  }
}

