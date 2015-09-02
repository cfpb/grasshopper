# Address Point API Spec

## Purpose
This document describes the Address Point Geocoding API, serving as a specification or "contract" for other users of this service.
In particular, it describes the endpoints that this service exposes, as well as the structure and format of the data that is exchanged when using this service.

### Protocols and data formats
The Address Point Geocoding Service exposes data through HTTP, accepting and returing data in JSON format. 
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
  "service": "grasshopper-addresspoints",
  "time": "2015-05-13T15:52:25.856Z",
  "host": "localhost"
}
```

**2. Single Point Geocode**

`GET  /addresses/points/<address>` where <address> is the address search

`GET /addresses/points/16410 N AR 94 Hwy Pea Ridge AR 72751`

The endpoint can also suggest alternative results, by passing the `suggest` parameter:

`GET /addresses/points/main+st?suggest=5`

This will return up to 5 candidates

`POST /addresses/points`

Payload: JSON object with the following structure:

* address: Address to geocode (String)

Example:

```json
{
  "address": "16410 N AR 94 Hwy Pea Ridge AR 72751"
}
```

Return: GeoJSON array that includes the point that resulted from geocoding, as well as other attributes including the address that was found as well as alternate address (if available).
The `match` field indicates how similar the address found is to the search input. A maximum value of `1` indicates equality (100% similarity).

```json
{
    "status": "OK",
    "input": "16410 N AR 94 Hwy Pea Ridge AR 72751",
    "features": [
        {
            "type": "Feature",
            "geometry": {
                "type": "Point",
                "coordinates": [
                    -94.15513718509897,
                    36.48036108322251
                ]
            },
            "properties": {
                "load_date": 1426878172094,
                "match": 1,
                "address": "16410 N AR 94 Hwy Pea Ridge AR 72751",
                "alt_address": ""
            }
        }
    ]
}
```



