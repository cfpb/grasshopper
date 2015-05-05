package tiger.model

case class AddressRange(start: Int, end: Int) {
  def count(): Int = end - start
}
