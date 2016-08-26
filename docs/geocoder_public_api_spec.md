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

`GET  /`

Returns a message with current status and date

```json
{
  parserStatus: {
    status: "OK",
    time: "2015-11-02T13:34:57.146499+00:00",
    upSince: "2015-10-28T16:26:10.444087+00:00",
    host: "896b22f803a0"
  }
}
```

**2. Single Geocode**

`GET /geocode/<address>` where:

 - `<address>` is the non-URL encoded input address to be found

The response follows the following structure.
One or more features may be returned, with the `source` field indicating what service is responsible for the geocoding.


**Example:**

`GET /geocode/200 President St Arkansas City AR 71630`

```json
{
  query: {
    "input": "200 President St Arkansas City AR 71630",
    "parts": {
      "city": "Arkansas City",
      "zip": "71630",
      "state": "AR",
      "streetName": "President St",
      "addressNumber": "200"
    }
  },
  "features": [
    {
      "type": "Feature",
      "geometry": {
        "type": "Point",
        "coordinates": [
          -91.19960153268617,
          33.60763673811005
        ]
      },
      "properties": {
        "RFROMHN": "100",
        "source": "census-tiger",
        "RTOHN": "498",
        "ZIPL": null,
        "FULLNAME": "President St",
        "LFROMHN": null,
        "LTOHN": null,
        "ZIPR": "71630",
        "STATE": "AR"
      }
    },
    {
      "type": "Feature",
      "geometry": {
        "type": "Point",
        "coordinates": [
          -91.19978780015629,
           33.608091616155995
        ]
      },
      "properties": {
        "address": "200 President St Arkansas City AR 71630",
        "alt_address": "",
        "source": "state-address-points"
      }
    }
  ]
}
```

**3. Batch Geocode**

`POST /geocode`

The request must send a file with one address string per line as `multipart/form-data`. For instance, using `curl`:

```
curl -v -F upload=@batch_addresses.csv http://localhost:31010/geocode
```

This endpoint will geocode the addresses in parallel and choose the best option from the available geocoders.
The response is a chunked response, and starts sending data to download as soon as the geocoding process begins.

Eventually a file is saved to disk, with the following format:

`input_address,latitude,longitude`

