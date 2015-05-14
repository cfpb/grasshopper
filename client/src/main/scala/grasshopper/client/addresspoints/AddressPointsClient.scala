package grasshopper.client.addresspoints

import java.io.IOException

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorFlowMaterializer
import com.typesafe.config.ConfigFactory
import feature.Feature
import grasshopper.client.ServiceClient
import grasshopper.client.addresspoints.model.AddressPointsStatus
import grasshopper.client.addresspoints.protocol.AddressPointsJsonProtocol

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Properties

object AddressPointsClient extends ServiceClient with AddressPointsJsonProtocol {

  override implicit val system: ActorSystem = ActorSystem("grasshopper-grasshopper.client-grasshopper.addresspoints")
  override implicit val materializer: ActorFlowMaterializer = ActorFlowMaterializer()
  override val config = ConfigFactory.load()

  lazy val host = Properties.envOrElse("GRASSHOPPER_ADDRESSPOINTS_HOST", config.getString("grasshopper.addresspoints.host"))
  lazy val port = Properties.envOrElse("GRASSHOPPER_ADDRESSPOINTS_PORT", config.getString("grasshopper.addresspoints.port"))

  def status(): Future[Either[String, AddressPointsStatus]] = {
    implicit val ec: ExecutionContext = system.dispatcher
    sendGetRequest(host, port.toInt, "/status").flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[AddressPointsStatus].map(Right(_))
        case BadRequest => Future.successful(Left("Bad Request"))
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"Request failed with status code ${response.status} and entity ${entity}}"
          Future.failed(new IOException(error))
        }
      }
    }
  }

  def geocode(address: String): Future[Either[String, List[Feature]]] = {
    implicit val ec: ExecutionContext = system.dispatcher
    sendGetRequest(host, port.toInt, s"/addresses/points/${address}").flatMap { response =>
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
