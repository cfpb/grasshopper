package addresspoints.protocol

import addresspoints.model.{ AddressInput, Status }
import spray.json.DefaultJsonProtocol

trait JsonProtocol extends DefaultJsonProtocol {
  implicit val statusFormat = jsonFormat2(Status.apply)
  implicit val addressInputFormat = jsonFormat2(AddressInput.apply)
}