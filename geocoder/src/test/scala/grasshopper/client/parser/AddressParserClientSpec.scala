package grasshopper.client.parser

import grasshopper.model.ParserStatus
import org.scalatest._

import scala.concurrent.Await
import scala.concurrent.duration._

class AddressParserClientSpec extends FlatSpec with MustMatchers {

  "A request to /status" must "return a status object" in {
    val maybeStatus: Either[String, ParserStatus] = Await.result(AddressParserClient.status, 10.seconds)
    maybeStatus match {
      case Right(status) =>
        status.status mustBe "OK"
      case Left(_) => throw new Exception("Error")
    }
  }
}
