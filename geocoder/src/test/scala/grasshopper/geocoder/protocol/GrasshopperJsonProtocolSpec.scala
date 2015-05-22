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

  //  "A geocode result" must "serialize from JSON" in {
  //    val geocodeResultStr = """
  //       {
  //         "status": "OK",
  //         "query": {
  //           "input": "1311 30th St Washington DC 20007",
  //           "parts": {
  //             "AddressNumber": "1311",
  //             "PlaceName": "washington",
  //             "StateName": "dc",
  //             "StreetName": "30th",
  //             "StreetNamePostType": "st",
  //             "ZipCode": "20007"
  //           }
  //         },
  //         "features": [
  //         {
  //           "service": "census",
  //           "data": {
  //              "type": "Feature",
  //              "geometry": {
  //                "type": "Point",
  //                "coordinates": [
  //                  -77.05908853531027,
  //                  38.90721451814751,
  //                  0
  //                ]
  //              },
  //              "properties": {
  //                "RFROMHN": "1301",
  //                "RTOHN": "1323",
  //                "ZIPL": "20007",
  //                "FULLNAME": "30th St NW",
  //                "LFROMHN": "1300",
  //                "LTOHN": "1318",
  //                "ZIPR": "20007",
  //                "STATE": "DC"
  //              }
  //           }
  //         },
  //         {
  //           "service": "addresspoints",
  //           "data": {
  //             "type": "Feature",
  //             "geometry": {
  //               "type": "Point",
  //               "coordinates": [
  //                 -77.05908853531027,
  //                 38.90721451814751,
  //                 0
  //               ]
  //             },
  //             "properties": {
  //               "address": "1311 30th st nw washington dc 20007",
  //               "alt_address": "",
  //               "load_date": 1426878185988
  //             }
  //           }
  //         }
  //         ]
  //       }
  //    """
  //
  //    val geocodeResult = geocodeResultStr.parseJson.convertTo[GeocodeResult]
  //    geocodeResult.status mustBe "OK"
  //    geocodeResult.query.parts.AddressNumber mustBe "1311"
  //    geocodeResult.query.parts.PlaceName mustBe "washington"
  //    geocodeResult.query.parts.StateName mustBe "dc"
  //    geocodeResult.query.parts.StreetName mustBe "30th"
  //    geocodeResult.query.parts.StreetNamePostType mustBe "st"
  //    geocodeResult.query.parts.ZipCode mustBe "20007"
  //    geocodeResult.features.size mustBe 2
  //    geocodeResult.features(0).service mustBe "census"
  //    geocodeResult.features(1).service mustBe "addresspoints"
  //    //geocodeResult.features(0).data.geometry mustBe geocodeResult.features(1).data.geometry
  //  }

}
