package grasshopper.client.parser.model

case class AddressPart(`type`: String, value: String)
case class ParsedAddress(parts: Array[AddressPart])

object ParsedAddress {
  def empty: ParsedAddress = ParsedAddress(Nil.toArray)
}
