package tiger.protocol

import tiger.model._
import spray.json.DefaultJsonProtocol

trait JsonProtocol extends DefaultJsonProtocol {
  implicit val statusFormat = jsonFormat3(Status.apply)
  implicit val addressInputFormat = jsonFormat1(AddressInput.apply)
}