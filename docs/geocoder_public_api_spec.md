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

 - `<address>` is the non-URL encoded input address to be found
 - `parseAddress` is true by default and triggers a validation (parsing) of the input string.

If `parseAddress` is `false`, a search will be performed on the input string as is.
In this case, only the addresspoints backend service will be used (i.e. no interpolation on census TIGER data).

The response follows the following structure.
One or more features may be returned, with the `service` field indicating what service is responsible for the geocoding,
and the `data` field containing the location in GeoJSON format.


**Example:**

`GET /geocode/200 President St Arkansas City AR 71630`

```json
{
  "status": "OK",
  "query": {
    "input": "200 President St Arkansas City AR 71630",
    "parts": {
      "city": "Arkansas City",
      "zip": "71630",
      "state": "AR",
      "streetName": "President St",
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
```

Each service has a status code that provides metadata about the geocode request performed:

- `OK` indicates that no errors resulted in the geocoding operation.
- `ADDRESS_NOT_FOUND` indicates that the geocoding engine could not find the address passed.
   The input string is correctly formed, but the address does not exist in the Grasshopper geocoding databases.
- `SERVICE_UNAVAILABLE` indicates an unexpected problem with the underlying service. The request may succeed if you try again.



**3. Batch Geocode**

`POST /geocode`

The request must send a file with one address string per line as `multipart/form-data`.
This endpoint will geocode the addresses in parallel and choose the best option from the available geocoders.
The response is a chunked response, and starts sending data to download as soon as the geocoding process begins.

Eventually a file is saved to disk, with the following format:

`input_address,latitude,longitude`

