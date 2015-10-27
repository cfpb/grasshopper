package grasshopper.client.census

import java.net.URLEncoder

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.typesafe.config.{ Config, ConfigFactory }
import grasshopper.client.ServiceClient
import grasshopper.client.model.ResponseError
import grasshopper.model.Status
import grasshopper.model.census.{ ParsedInputAddress, CensusResult }
import grasshopper.protocol.StatusJsonProtocol
import grasshopper.protocol.census.CensusJsonProtocol
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Properties

object CensusClient extends ServiceClient with StatusJsonProtocol with CensusJsonProtocol {
  override val config: Config = ConfigFactory.load()

  lazy val host = Properties.envOrElse("GRASSHOPPER_CENSUS_HOST", config.getString("grasshopper.client.census.host"))
  lazy val port = Properties.envOrElse("GRASSHOPPER_CENSUS_PORT", config.getString("grasshopper.client.census.port"))

  def status: Future[Either[ResponseError, Status]] = {
    implicit val ec: ExecutionContext = system.dispatcher
    sendGetRequest("/").flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[Status].map(Right(_))
        case _ => sendResponseError(response)
      }
    }
  }

  def geocode(address: ParsedInputAddress): Future[Either[ResponseError, CensusResult]] = {
    implicit val ec: ExecutionContext = system.dispatcher
    val streetName = URLEncoder.encode(address.streetName, "UTF-8")
    val state = URLEncoder.encode(address.state, "UTF-8")
    val url = s"/census/addrfeat?number=${address.addressNumber}&streetName=${streetName}&zipCode=${address.zipCode}&state=${state}"
    sendGetRequest(url).flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[CensusResult].map(Right(_))
        case _ => sendResponseError(response)
      }
    }
  }

}
