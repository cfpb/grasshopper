package grasshopper.geocoder.api

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.coding.{ Deflate, Gzip, NoCoding }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorFlowMaterializer
import com.typesafe.config.Config
import com.typesafe.scalalogging.Logger
import grasshopper.client.addresspoints.AddressPointsClient
import grasshopper.client.addresspoints.model.AddressPointsStatus
import grasshopper.client.census.CensusClient
import grasshopper.client.census.model.CensusStatus
import grasshopper.client.parser.AddressParserClient
import grasshopper.client.parser.model.ParserStatus
import grasshopper.geocoder.model.GeocodeStatus
import grasshopper.geocoder.protocol.GrasshopperJsonProtocol
import org.slf4j.LoggerFactory
import scala.async.Async.{ async, await }
import scala.concurrent.{ ExecutionContextExecutor, Future }

trait Service extends GrasshopperJsonProtocol {
  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor
  implicit val materializer: ActorFlowMaterializer

  def config: Config

  val logger: LoggingAdapter

  lazy val log = Logger(LoggerFactory.getLogger("grashopper-geocoder"))

  val routes = {
    path("status") {
      def addressPointsStatus = AddressPointsClient.status.map(s => s.right.getOrElse(AddressPointsStatus.empty))
      def censusStatus = CensusClient.status.map(s => s.right.getOrElse(CensusStatus.empty))
      def parserStatus = AddressParserClient.status.map(s => s.right.getOrElse(ParserStatus.empty))
      val fStatus: Future[GeocodeStatus] = async {
        val as = addressPointsStatus
        val cs = censusStatus
        val ps = parserStatus
        GeocodeStatus(await(as), await(cs), await(ps))
      }

      encodeResponseWith(NoCoding, Gzip, Deflate) {
        complete {
          fStatus.map { s =>
            ToResponseMarshallable(s)
          }
        }
      }
    }
  }

}
