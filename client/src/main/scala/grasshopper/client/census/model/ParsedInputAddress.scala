package grasshopper.client.census.model

case class ParsedInputAddress(number: Int, streetName: String, zipCode: Int, state: String) {
  override def toString(): String = {
    s"${number} ${streetName} ${state} ${zipCode}"
  }
  def isEmpty: Boolean = this.number == 0 && this.streetName == "" && this.zipCode == 0 && this.state == ""
}

object ParsedInputAddress {
  def empty: ParsedInputAddress = ParsedInputAddress(0, "", 0, "")
}
