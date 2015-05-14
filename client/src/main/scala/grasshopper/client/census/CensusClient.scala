package grasshopper.client.census

import java.io.IOException

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.unmarshalling.Unmarshal
import feature.Feature
import grasshopper.client.census.protocol.CensusJsonProtocol
import io.geojson.FeatureJsonProtocol._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.typesafe.config.{ ConfigFactory, Config }
import grasshopper.client.ServiceClient
import grasshopper.client.census.model.{ ParsedInputAddress, CensusStatus }
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Properties

object CensusClient extends ServiceClient with CensusJsonProtocol {
  override val config: Config = ConfigFactory.load()

  lazy val host = Properties.envOrElse("GRASSHOPPER_CENSUS_HOST", config.getString("grasshopper.client.census.host"))
  lazy val port = Properties.envOrElse("GRASSHOPPER_CENSUS_PORT", config.getString("grasshopper.client.census.port"))

  def status(): Future[Either[String, CensusStatus]] = {
    implicit val ec: ExecutionContext = system.dispatcher
    sendGetRequest(host, port.toInt, "/status").flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[CensusStatus].map(Right(_))
        case BadRequest => Future.successful(Left("Bad Request"))
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"Request failed with status code ${response.status} and entity ${entity}}"
          Future.failed(new IOException(error))
        }
      }
    }
  }

  def geocode(address: ParsedInputAddress): Future[Either[String, List[Feature]]] = {
    implicit val ec: ExecutionContext = system.dispatcher
    val url = s"/census/addrfeat?number=${address.number}&streetName=${address.streetName}&zipCode=${address.zipCode}&state=${address.state}"
    sendGetRequest(host, port.toInt, url).flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[List[Feature]].map(Right(_))
        case BadRequest => Future.successful(Left("Bad Request"))
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"Request failed with status code ${response.status} and entity ${entity}}"
          Future.failed(new IOException(error))
        }
      }
    }

  }
}
