package grasshopper.census.model

case class ParsedAddressInput(number: Int, streetName: String, zipCode: Int, state: String) {
  override def toString(): String = {
    s"${number} ${streetName} ${state} ${zipCode}"
  }
}
