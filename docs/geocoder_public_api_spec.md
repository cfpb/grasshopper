# Geocoder API Spec

## Purpose
This document describes the Public Geocoding Service API, serving as a specification or "contract" for other users of this service.
This API is the default public API for the Grasshopper project. In particular, it describes the endpoints that this service exposes, as well as the structure and format of the data that is exchanged when using this service.

### Protocols and data formats
The Public Geocoding Service API exposes data through HTTP, accepting and returing data in JSON format.
In particular, all requests need to specify their content type as appropriate:

```
Content-Type: application/json
```

Requests that don't specify their content type will be rejected

### API Endpoints

**1. Status**

`GET  /status`

Returns a message with current status and date

```json
{
  "status": "OK",
  "service": "grasshopper-geocoder",
  "time": "2015-05-13T15:52:25.856Z",
  "host": "localhost"
}
```

**2. Single Geocode**

`GET /geocode/<address>?parseAddress=<parseAddress>` where:

 - `<address>` is the input address to be found
 - `parseAddress` is true by default and triggers a validation (parsing) of the input string.

If `parseAddress` is `false`, a search will be performed on the input string as is.
In this case, only the addresspoints backend service will be used (i.e. no interpolation on census TIGER data).

The response follows the following structure.
One or more features may be returned, with the `service` field indicating what service is responsible for the geocoding,
and the `data` field containing the location in GeoJSON format.


**Example:**

GET /geocode/1311+30th+St+NW+Washington+DC+20007

```json
{
  "status": "OK",
  "query": {
    "input": "1311 30th St Washington DC 20007",
    "parts": {
      "AddressNumber": "1311",
      "PlaceName": "washington",
      "StateName": "dc",
      "StreetName": "30th",
      "StreetNamePostType": "st",
      "ZipCode": "20007"
    }
  },
  "features": [
      {
        "service": "census",
        "data": {
          "type": "Feature",
          "geometry": {
            "type": "Point",
            "coordinates": [
              -77.05908853531027,
              38.90721451814751,
              0
            ]
          },
          "properties": {
            "RFROMHN": "1301",
            "RTOHN": "1323",
            "ZIPL": "20007",
            "FULLNAME": "30th St NW",
            "LFROMHN": "1300",
            "LTOHN": "1318",
            "ZIPR": "20007",
            "STATE": "DC"
          }
        }
      },
      {
        "service": "addresspoints",
        "data": {
          "type": "Feature",
          "geometry": {
            "type": "Point",
              "coordinates": [
                 -77.05908853531027,
                 38.90721451814751,
                  0
              ]
          },
          "properties": {
            "address": "1311 30th st nw washington dc 20007",
            "alt_address": "",
            "load_date": 1426878185988
          }
        }
      }
  ]
}
```

* status: Contains metadata on the request. The following status codes can be returned:

- `OK` indicates that no errors resulted in the geocoding operation.
   A list of underlying services with a list of potential matches will be included in the response.
- `ADDRESS_NOT_FOUND` indicates that the geocoding engine could not find the address passed.
   The input string is correctly formed, but the address does not exist in the Grasshopper geocoding databases.
- `ADDRESS_INCOMPLETE` indicates an incomplete address. More information needs to be provided in order to resolve a location.
- `UNKNOWN_ERROR` indicates an unexpected problem with the service. The request may succeed if you try again.



**3. Batch Geocode**