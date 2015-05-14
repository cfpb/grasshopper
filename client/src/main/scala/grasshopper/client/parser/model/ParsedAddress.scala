package grasshopper.client.parser.model

case class AddressPart(
  AddressNumber: String,
  PlaceName: String,
  StateName: String,
  StreetName: String,
  StreetNamePostType: String,
  ZipCode: String
)
case class ParsedAddress(input: String, parts: AddressPart)
