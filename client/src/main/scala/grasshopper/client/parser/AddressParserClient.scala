package grasshopper.client.parser

import java.io.IOException
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.typesafe.config.ConfigFactory
import grasshopper.client.ServiceClient
import grasshopper.client.parser.model.{ ParserStatus, ParsedAddress }
import grasshopper.client.parser.protocol.ParserJsonProtocol
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Properties

object AddressParserClient extends ServiceClient with ParserJsonProtocol {
  override val config = ConfigFactory.load()

  lazy val host = Properties.envOrElse("GRASSHOPPER_PARSER_HOST", config.getString("grasshopper.client.parser.host"))
  lazy val port = Properties.envOrElse("GRASSHOPPER_PARSER_PORT", config.getString("grasshopper.client.parser.port"))

  def status(): Future[Either[String, ParserStatus]] = {
    implicit val ec: ExecutionContext = system.dispatcher
    sendGetRequest(host, port.toInt, "/status").flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[ParserStatus].map(Right(_))
        case BadRequest => Future.successful(Left("Bad Request"))
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"Request failed with status code ${response.status} and entity ${entity}}"
          Future.failed(new IOException(error))
        }
      }
    }
  }

  def parse(address: String): Future[Either[String, ParsedAddress]] = {
    implicit val ec: ExecutionContext = system.dispatcher
    sendGetRequest(host, port.toInt, s"/parse?address=${address}").flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[ParsedAddress].map(Right(_))
        case BadRequest => Future.successful(Left("Bad Request"))
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"Request failed with status code ${response.status} and entity ${entity}}"
          Future.failed(new IOException(error))
        }
      }

    }
  }

}
