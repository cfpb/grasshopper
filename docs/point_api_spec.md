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

`GET  /status`

Returns a message with current status and date

```json
{
    "status": "OK",
    "time": "Wed Feb 18 10:30:55 EST 2015"
}
```

`POST /address/point`

Payload: JSON object with the following structure:

* id: Identifier for the record (Integer)
* address: Address to geocode (String)

Example:

```json
{
  "id": 1,
  "address": "16410 N AR 94 Hwy Pea Ridge 72751 AR"
}
```

Return: GeoJSON object that includes the id sent in the request as a field, and the point that resulted from geocoding, as well as other attributes including the address that was found.


```json
{
    "type": "Feature",
    "geometry": {
        "type": "Point",
        "coordinates": [
            -94.15513718509897,
            36.48036108322251,
            0
        ]
    },
    "properties": {
        "id":1,
        "AID": 28101,
        "ADDRESS": "16410 N AR 94 Hwy Pea Ridge 72751 AR"
    }
}
```



