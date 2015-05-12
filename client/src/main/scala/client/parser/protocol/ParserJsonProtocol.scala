package grasshopper.client.parser.protocol

import grasshopper.client.parser.model.ParserStatus
import spray.json.DefaultJsonProtocol

trait ParserJsonProtocol extends DefaultJsonProtocol {
  implicit val statusFormat = jsonFormat4(ParserStatus.apply)
}
