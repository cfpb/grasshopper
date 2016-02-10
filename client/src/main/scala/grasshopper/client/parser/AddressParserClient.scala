package grasshopper.client.parser

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.typesafe.config.ConfigFactory
import grasshopper.client.ServiceClient
import grasshopper.client.model.ResponseError
import grasshopper.client.parser.model.{ ParserStatus, ParsedAddress }
import grasshopper.client.parser.protocol.ParserJsonProtocol
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Properties
import java.net.URLEncoder

object AddressParserClient extends ServiceClient with ParserJsonProtocol {
  override val config = ConfigFactory.load()

  lazy val host = config.getString("grasshopper.client.parser.host")
  lazy val port = config.getString("grasshopper.client.parser.port")

  def status: Future[Either[ResponseError, ParserStatus]] = {
    implicit val ec: ExecutionContext = system.dispatcher
    sendGetRequest("/").flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[ParserStatus].map(Right(_))
        case _ => sendResponseError(response)
      }
    }
  }

  def parse(addressString: String): Future[Either[ResponseError, ParsedAddress]] = {
    implicit val ec: ExecutionContext = system.dispatcher

    //FIXME: Use a URL builder, and don't hard-code profile
    val encodedAddress = URLEncoder.encode(addressString, "UTF-8")
    val url = s"/parse?profile=grasshopper&address=$encodedAddress"
    sendGetRequest(url).flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[ParsedAddress].map(Right(_))
        case _ => sendResponseError(response)
      }
    }
  }

}
