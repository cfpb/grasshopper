package grasshopper.model.census

case class ParsedInputAddress(addressNumber: String, streetName: String, city: String, zipCode: String, state: String) {
  override def toString(): String = {
    s"${addressNumber} ${streetName} ${state} ${zipCode}"
  }
  def isEmpty: Boolean = this.addressNumber == "" && this.streetName == "" && this.city == "" && this.zipCode == "" && this.state == ""
}

object ParsedInputAddress {
  def empty: ParsedInputAddress =
    ParsedInputAddress("", "", "", "", "")
}
