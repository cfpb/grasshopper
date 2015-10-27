package grasshopper.census.http

import java.net.InetAddress
import java.time.Instant

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.coding.{ Deflate, Gzip, NoCoding }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import com.typesafe.scalalogging.Logger
import feature.Feature
import grasshopper.census.model.{ CensusResult, ParsedInputAddress, Status }
import grasshopper.census.protocol.CensusJsonProtocol
import grasshopper.census.search.CensusGeocode
import org.elasticsearch.client.Client
import org.slf4j.LoggerFactory
import spray.json._

import scala.concurrent.ExecutionContextExecutor
import scala.util.{ Failure, Success, Try }

trait HttpService extends CensusJsonProtocol with CensusGeocode {
  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: ActorMaterializer
  implicit val client: Client

  def config: Config

  val logger: LoggingAdapter

  override lazy val log = Logger(LoggerFactory.getLogger("grasshopper-census"))

  val routes = {
    pathSingleSlash {
      get {
        encodeResponseWith(NoCoding, Gzip, Deflate) {
          complete {
            val now = Instant.now.toString
            val host = InetAddress.getLocalHost.getHostName
            val status = Status("OK", "grasshopper-census", now, host)
            log.debug(status.toJson.toString())
            ToResponseMarshallable(status)
          }
        }
      }
    } ~
      pathPrefix("census") {
        path("addrfeat") {
          get {
            parameters(
              'number.as[String] ? "",
              'streetName.as[String] ? "",
              'zipCode.as[String] ? "",
              'state.as[String] ? ""
            ) { (number, streetName, zipCode, state) =>
                val addressInput = ParsedInputAddress(number, streetName, zipCode, state)
                encodeResponseWith(NoCoding, Gzip, Deflate) {
                  geocodeLines(addressInput, 1)
                }
              }
          } ~
            post {
              encodeResponseWith(NoCoding, Gzip, Deflate) {
                entity(as[String]) { json =>
                  val tryAddressInput = Try(json.parseJson.convertTo[ParsedInputAddress])
                  tryAddressInput match {
                    case Success(a) => geocodeLines(a, 1)
                    case Failure(e) =>
                      log.error(e.getLocalizedMessage)
                      complete(BadRequest)
                  }
                }
              }
            }
        }
      }
  }

  private def geocodeLines(addressInput: ParsedInputAddress, count: Int): StandardRoute = {

    def notFoundRoute(pts: Array[Feature]): StandardRoute = {
      complete(ToResponseMarshallable(CensusResult("ADDRESS_NOT_FOUND", pts)))
    }

    val points = geocodeLine(client, "census", "addrfeat", addressInput, count) getOrElse (Nil.toArray)
    if (points.length > 0) {
      val pts = points.filter(p => p.geometry.centroid.x != 0 && p.geometry.centroid.y != 0)
      if (pts.length > 0) {
        complete(ToResponseMarshallable(CensusResult("OK", points)))
      } else {
        notFoundRoute(pts)
      }
    } else {
      val pts: Array[Feature] = Nil.toArray
      notFoundRoute(pts)
    }

  }
}
