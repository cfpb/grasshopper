package grasshopper.client.addresspoints

import java.net.URLEncoder
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.typesafe.config.ConfigFactory
import grasshopper.client.ServiceClient
import grasshopper.client.addresspoints.model.{ AddressPointsResult, AddressPointsStatus }
import grasshopper.client.addresspoints.protocol.AddressPointsJsonProtocol
import grasshopper.client.model.ResponseError
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Properties

object AddressPointsClient extends ServiceClient with AddressPointsJsonProtocol {
  override val config = ConfigFactory.load()

  lazy val host = Properties.envOrElse("GRASSHOPPER_ADDRESSPOINTS_HOST", config.getString("grasshopper.client.addresspoints.host"))
  lazy val port = Properties.envOrElse("GRASSHOPPER_ADDRESSPOINTS_PORT", config.getString("grasshopper.client.addresspoints.port"))

  def status: Future[Either[ResponseError, AddressPointsStatus]] = {
    implicit val ec: ExecutionContext = system.dispatcher
    sendGetRequest(Uri("/status")).flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[AddressPointsStatus].map(Right(_))
        case _ => sendResponseError(response)
      }
    }
  }

  def geocode(address: String): Future[Either[ResponseError, AddressPointsResult]] = {
    implicit val ec: ExecutionContext = system.dispatcher
    val addr = if (address.contains("?suggest=")) address else URLEncoder.encode(address, "UTF-8")
    val url = Uri(s"/addresses/points/${addr}")
    sendGetRequest(url).flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[AddressPointsResult].map(Right(_))
        case _ => sendResponseError(response)
      }
    }
  }

}
