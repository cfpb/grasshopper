package grasshopper.census.model

case class ParsedInputAddress(addressNumber: String, streetName: String, zipCode: Int, state: String) {
  override def toString(): String = {
    s"${addressNumber} ${streetName} ${state} ${zipCode}"
  }
}

object ParsedInputAddress {
  def empty: ParsedInputAddress =
    ParsedInputAddress("", "", 0, "")
}
