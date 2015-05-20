package grasshopper.client.census.model

case class ParsedInputAddress(number: Int, streetName: String, zipCode: Int, state: String) {
  override def toString(): String = {
    s"${number} ${streetName} ${state} ${zipCode}"
  }
}

object ParsedInputAddress {
  def empty: ParsedInputAddress =
    ParsedInputAddress(0, "", 0, "")
}
