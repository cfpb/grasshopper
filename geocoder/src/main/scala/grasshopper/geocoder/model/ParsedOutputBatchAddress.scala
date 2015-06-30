package grasshopper.geocoder.model

import grasshopper.client.census.model.ParsedInputAddress

case class ParsedOutputBatchAddress(input: String, parsed: ParsedInputAddress)
