package grasshopper.client.parser.protocol

import grasshopper.client.parser.model.{ ParserStatus, AddressPart, ParsedAddress }
import spray.json.DefaultJsonProtocol

trait ParserJsonProtocol extends DefaultJsonProtocol {
  implicit val parserStatusFormat = jsonFormat4(ParserStatus.apply)
  implicit val addressPartFormat = jsonFormat2(AddressPart.apply)
  implicit val parsedAddressFormat = jsonFormat2(ParsedAddress.apply)
}
