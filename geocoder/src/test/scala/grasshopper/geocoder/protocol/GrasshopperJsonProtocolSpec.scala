package grasshopper.geocoder.protocol

import grasshopper.client.parser.model.{ AddressPart, ParserStatus }
import grasshopper.geocoder.model.{ GeocodeResponse, GeocodeStatus }
import grasshopper.model.AddressSearchResult
import org.scalatest.{ MustMatchers, FlatSpec }
import spray.json._

class GrasshopperJsonProtocolSpec extends FlatSpec with MustMatchers with GrasshopperJsonProtocol {

  "A Status" must "deserialize from JSON" in {
    val statusStr =
      """
        {
          "parserStatus": {
            "status": "OK",
            "time": "2015-05-21T14:24:27.112803+00:00",
            "upSince": "2015-05-08T20:16:32.264973+00:00",
            "host": "cfa96f3d0de0"
          }
        }
      """.stripMargin

    val geocoderStatus = statusStr.parseJson.convertTo[GeocodeStatus]
    geocoderStatus.parserStatus.host mustBe "cfa96f3d0de0"
  }

  it must "serialize to JSON" in {
    val parserStatus = ParserStatus("OK", "2015-05-21T14:24:27.112803+00:00", "2015-05-08T20:16:32.264973+00:00", "cfa96f3d0de0")
    val geocodeStatus = GeocodeStatus(parserStatus)
    geocodeStatus.toJson.toString.parseJson.convertTo[GeocodeStatus] mustBe geocodeStatus
  }

  "AddressPointService" must "serialize from JSON" in {
    val addressPointsServiceStr =
      """
           {
             "features": [
               {
                 "type": "Feature",
                 "geometry": {
                   "type": "Point",
                     "coordinates": [
                       -92.48266322840489,
                        33.77129525636826
                     ]
                 },
                 "properties": {
                   "address": "1489 Chambersville Rd Thornton AR 71766",
                   "alt_address": "",
                   "load_date": 1426878178730
                 }
               }
             ]
           }
        """.stripMargin
    val addressPointService = addressPointsServiceStr.parseJson.convertTo[AddressSearchResult]
    addressPointService.features.size mustBe 1
    addressPointService.features(0).get("load_date").getOrElse(0) mustBe 1426878178730L
  }

  "A geocode result" must "serialize from JSON" in {
    val geocodeResponseJSON = """
        {
          "input": "200 President St Arkansas City AR 71630",
          "parts": [
            {"code": "address_number_full", "value": "200"},
            {"code": "street_name_full", "value": "President St"},
            {"code": "city_name", "value": "Arkansas City"},
            {"code": "state_name", "value": "AR"},
            {"code": "zip_code", "value": "71630"}
          ],
          "features": [{
            "type": "Feature",
            "geometry": {
              "type": "Point",
              "coordinates": [-91.19978780015629, 33.608091616155995]
            },
            "properties": {
              "address": "200 President St Arkansas City AR 71630",
              "alt_address": "",
              "load_date": 1426878185988
            }
          },
          {
            "type": "Feature",
            "geometry": {
              "type": "Point",
              "coordinates": [-91.19960153268617, 33.60763673811005]
            },
            "properties": {
              "RFROMHN": "100",
              "RTOHN": "498",
              "ZIPL": "",
              "FULLNAME": "President St",
              "LFROMHN": "",
              "LTOHN": "",
              "ZIPR": "71630",
              "STATE": "AR"
            }
          }]
        }
      """

    val geocodeResponse = geocodeResponseJSON.parseJson.convertTo[GeocodeResponse]
    val parts = geocodeResponse.parts
    val features = geocodeResponse.features

    geocodeResponse.input mustBe "200 President St Arkansas City AR 71630"

    parts.length mustBe 5
    parts(0) mustBe AddressPart("address_number_full", "200")
    parts(1) mustBe AddressPart("street_name_full", "President St")
    parts(2) mustBe AddressPart("city_name", "Arkansas City")
    parts(3) mustBe AddressPart("state_name", "AR")
    parts(4) mustBe AddressPart("zip_code", "71630")

    features.length mustBe 2
  }

}
