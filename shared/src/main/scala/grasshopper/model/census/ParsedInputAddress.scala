package grasshopper.model.census

case class ParsedInputAddress(addressNumber: String, streetName: String, zipCode: String, state: String) {
  override def toString(): String = {
    s"${addressNumber} ${streetName} ${state} ${zipCode}"
  }
}

object ParsedInputAddress {
  def empty: ParsedInputAddress =
    ParsedInputAddress("", "", "", "")
}
