package grasshopper.geocoder.protocol

import grasshopper.client.addresspoints.model.{ AddressPointsResult, AddressPointsStatus }
import grasshopper.client.census.model.CensusStatus
import grasshopper.client.parser.model.ParserStatus
import grasshopper.geocoder.model.{ GeocodeStatus, GeocodeResult }
import org.scalatest.{ MustMatchers, FlatSpec }
import spray.json._

class GrasshopperJsonProtocolSpec extends FlatSpec with MustMatchers with GrasshopperJsonProtocol {

  "A Status" must "deserialize from JSON" in {
    val statusStr =
      """
        {
          "addressPointsStatus": {
            "status": "OK",
            "service": "grasshopper-addresspoints",
            "time": "2015-05-21T14:24:22.477Z",
            "host": "localhost"
          },
          "censusStatus": {
            "status": "SERVICE_UNAVAILABLE",
            "service": "grasshopper-addresspoints",
            "time": "2015-05-21T14:24:22.102Z",
            "host": ""
          },
          "parserStatus": {
            "status": "OK",
            "time": "2015-05-21T14:24:27.112803+00:00",
            "upSince": "2015-05-08T20:16:32.264973+00:00",
            "host": "cfa96f3d0de0"
          }
        }
      """.stripMargin

    val geocoderStatus = statusStr.parseJson.convertTo[GeocodeStatus]
    geocoderStatus.addressPointsStatus.status mustBe "OK"
    geocoderStatus.censusStatus.status mustBe "SERVICE_UNAVAILABLE"
    geocoderStatus.parserStatus.host mustBe "cfa96f3d0de0"
  }

  it must "serialize to JSON" in {
    val addressPointStatus = AddressPointsStatus("OK", "grasshopper-addresspoints", "2015-05-21T14:24:22.477Z", "localhost")
    val censusStatus = CensusStatus("SERVICE_UNAVAILABLE", "grasshopper-addresspoints", "2015-05-21T14:24:22.102Z", "")
    val parserStatus = ParserStatus("OK", "2015-05-21T14:24:27.112803+00:00", "2015-05-08T20:16:32.264973+00:00", "cfa96f3d0de0")
    val geocodeStatus = GeocodeStatus(addressPointStatus, censusStatus, parserStatus)
    geocodeStatus.toJson.toString.parseJson.convertTo[GeocodeStatus] mustBe geocodeStatus
  }

  "AddressPointService" must "serialize from JSON" in {
    val addressPointsServiceStr =
      """
           {
             "status": "OK",
             "features": [
               {
                 "type": "Feature",
                 "geometry": {
                   "type": "Point",
                     "coordinates": [
                       -92.48266322840489,
                        33.77129525636826,
                         0
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
    val addressPointService = addressPointsServiceStr.parseJson.convertTo[AddressPointsResult]
    addressPointService.features.size mustBe 1
    addressPointService.features(0).get("load_date").getOrElse(0) mustBe 1426878178730L
  }

  "A geocode result" must "serialize from JSON" in {
    val geocodeResultStr = """
        {
          "status": "OK",
          "query": {
            "input": "200 President St Arkansas City AR 71630",
            "parts": {
              "streetName": "President St",
              "state": "AR",
              "city": "City",
              "zip": "71630",
              "addressNumber": "200"
            }
          },
          "addressPointsService": {
            "status": "OK",
            "features": [{
              "type": "Feature",
              "geometry": {
                "type": "Point",
                "coordinates": [-91.19978780015629, 33.608091616155995, 0.0]
              },
              "properties": {
                "address": "200 President St Arkansas City AR 71630",
                "alt_address": "",
                "load_date": 1426878185988
              }
            }]
          },
          "censusService": {
            "status": "OK",
            "features": [{
              "type": "Feature",
              "geometry": {
                "type": "Point",
                "coordinates": [-91.19960153268617, 33.60763673811005, 0.0]
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
        }
      """

    val geocodeResult = geocodeResultStr.parseJson.convertTo[GeocodeResult]
    geocodeResult.status mustBe "OK"
    geocodeResult.query.parts.addressNumber mustBe "200"
    geocodeResult.query.parts.state mustBe "AR"
    geocodeResult.query.parts.streetName mustBe "President St"
    geocodeResult.query.parts.zip mustBe "71630"

  }

}
