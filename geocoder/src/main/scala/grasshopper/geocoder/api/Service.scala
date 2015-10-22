package grasshopper.geocoder.api

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.coding.{ Deflate, Gzip, NoCoding }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.MediaTypes.`text/csv`
import akka.http.scaladsl.model.Multipart.FormData
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.io.Framing
import akka.util.ByteString
import com.typesafe.config.Config
import com.typesafe.scalalogging.Logger
import grasshopper.model.Status
import grasshopper.client.addresspoints.AddressPointsClient
import grasshopper.client.addresspoints.model.{ AddressPointsResult, AddressPointsStatus }
import grasshopper.client.census.CensusClient
import grasshopper.client.census.model.{ CensusResult, ParsedInputAddress }
import grasshopper.client.parser.AddressParserClient
import grasshopper.client.parser.model.{ ParsedAddress, ParserStatus }
import grasshopper.client.protocol.ClientJsonProtocol
import grasshopper.geocoder.model.{ AddressPointsGeocodeBatchResult, CensusGeocodeBatchResult, GeocodeResult, GeocodeStatus }
import grasshopper.geocoder.protocol.GrasshopperJsonProtocol
import org.slf4j.LoggerFactory

import scala.async.Async.{ async, await }
import scala.concurrent.{ ExecutionContextExecutor, Future }

trait Service extends GrasshopperJsonProtocol with ClientJsonProtocol {
  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor
  implicit val materializer: ActorMaterializer

  def config: Config

  val logger: LoggingAdapter

  lazy val log = Logger(LoggerFactory.getLogger("grashopper-geocoder"))

  val routes = {
    pathSingleSlash {
      val fStatus: Future[GeocodeStatus] = async {
        val as = AddressPointsClient.status.map(s => s.right.getOrElse(AddressPointsStatus.empty))
        val cs = CensusClient.status.map(s => s.right.getOrElse(Status.empty))
        val ps = AddressParserClient.status.map(s => s.right.getOrElse(ParserStatus.empty))
        GeocodeStatus(await(as), await(cs), await(ps))
      }

      encodeResponseWith(NoCoding, Gzip, Deflate) {
        complete {
          fStatus.map { s =>
            ToResponseMarshallable(s)
          }
        }
      }
    } ~
      path("geocode") {
        post {
          entity(as[FormData]) { formData =>
            complete {
              val source = formData.parts
                .mapAsync(4) { bodyPart =>
                  bodyPart.entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map { contents =>
                    contents
                  }
                }

              val linesStream = source.via(
                Framing.delimiter(
                  ByteString("\n"),
                  maximumFrameLength = 100,
                  allowTruncation = true
                )
              ).map(_.utf8String)

              linesStream
                .via(GeocodeFlows.geocode)

              val geocodeFlow = linesStream
                .via(GeocodeFlows.geocode)
                .map {
                  case a: AddressPointsGeocodeBatchResult =>
                    a.toCsv
                  case c: CensusGeocodeBatchResult =>
                    c.toCsv
                }

              val geocodeByteStream = geocodeFlow.map(s => ByteString(s))
              HttpEntity.Chunked.fromData(`text/csv`, geocodeByteStream)
            }
          }
        }
      } ~
      path("geocode" / Segment) { address =>
        val fParsed: Future[(ParsedAddress, ParsedInputAddress)] = async {
          val addr = await(AddressParserClient.standardize(address))
          if (addr.isLeft) {
            log.error(addr.left.get.desc)
            (ParsedAddress.empty, ParsedInputAddress.empty)
          } else {
            val parsedAddress = addr.right.getOrElse(ParsedAddress.empty)
            val parsedInputAddress = ParsedInputAddress(
              parsedAddress.parts.addressNumber.toInt,
              parsedAddress.parts.streetName,
              parsedAddress.parts.zip,
              parsedAddress.parts.state
            )
            (parsedAddress, parsedInputAddress)
          }
        }

        val fGeocoded = async {
          val parsed = await(fParsed)
          val parsedAddress = parsed._1
          val parsedInputAddress = parsed._2

          val ptGeocode = await(AddressPointsClient.geocode(address))
          val addressPointGeocode: AddressPointsResult =
            if (ptGeocode.isLeft) {
              log.error(ptGeocode.left.get.desc)
              AddressPointsResult.error
            } else {
              ptGeocode.right.getOrElse(AddressPointsResult.empty)
            }

          val censusPointGeocode: CensusResult =
            if (parsedInputAddress.isEmpty) {
              CensusResult.empty
            } else {
              val cGeocode = await(CensusClient.geocode(parsedInputAddress))
              if (cGeocode.isLeft) {
                log.error(cGeocode.left.get.desc)
                CensusResult.error
              } else {
                cGeocode.right.getOrElse(CensusResult.empty)
              }
            }
          GeocodeResult("OK", parsedAddress, addressPointGeocode, censusPointGeocode)
        }

        encodeResponseWith(NoCoding, Gzip, Deflate) {
          complete {
            fGeocoded.map { g =>
              ToResponseMarshallable(g)
            }
          }
        }

      }

  }

}
