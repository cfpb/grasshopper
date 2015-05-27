package grasshopper.client.parser.model

case class AddressPart(
  addressNumber: String,
  city: String,
  state: String,
  streetName: String,
  zip: String
)
case class ParsedAddress(input: String, parts: AddressPart)

object ParsedAddress {
  def empty: ParsedAddress = ParsedAddress("", AddressPart("", "", "", "", ""))
}
