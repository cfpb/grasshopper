package grasshopper.client.census.model

case class ParsedInputAddress(number: Int, streetName: String, zipCode: String, state: String) {
  override def toString(): String = {
    s"${number} ${streetName} ${state} ${zipCode}"
  }
  def isEmpty: Boolean = this.number == 0 && this.streetName == "" && this.zipCode == "" && this.state == ""
}

object ParsedInputAddress {
  def empty: ParsedInputAddress = ParsedInputAddress(0, "", "", "")
}
