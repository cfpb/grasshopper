package grasshopper.model

case class SearchableAddress(addressNumber: String, streetName: String, city: String, zipCode: String, state: String) {
  override def toString(): String = {
    s"${addressNumber} ${streetName} ${city} ${state} ${zipCode}"
  }
  def isEmpty: Boolean = this.addressNumber == "" && this.streetName == "" && this.city == "" && this.zipCode == "" && this.state == ""
}

object SearchableAddress {
  def empty: SearchableAddress =
    SearchableAddress("", "", "", "", "")
}
