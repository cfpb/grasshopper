package grasshopper.geocoder.search

import grasshopper.geocoder.model.GeocodeResponse
import grasshopper.geocoder.search.addresspoints.AddressPointsGeocode
import grasshopper.geocoder.search.census.CensusGeocode

trait Geocode extends AddressPointsGeocode with CensusGeocode {

  def geocode(address: String): GeocodeResponse = {

    GeocodeResponse.empty

  }

}
