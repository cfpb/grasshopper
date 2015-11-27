package grasshopper.client.parser.model

object ParsedAddress {
  def empty: ParsedAddress = ParsedAddress("", List.empty)
}

case class AddressPart(code: String, value: String)
case class ParsedAddress(input: String, parts: List[AddressPart])
