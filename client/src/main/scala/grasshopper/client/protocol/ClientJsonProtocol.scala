package grasshopper.client.protocol

import grasshopper.client.model.ResponseError
import spray.json.DefaultJsonProtocol

trait ClientJsonProtocol extends DefaultJsonProtocol {
  implicit val responseErrorFormat = jsonFormat1(ResponseError.apply)
}
