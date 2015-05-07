# TIGER Line API Spec

## Purpose
This document describes the TIGER Line Geocoding API, serving as a specification or "contract" for other users of this service.
In particular, it describes the endpoints that this service exposes, as well as the structure and format of the data that is exchanged when using this service.

### Protocols and data formats
The TIGER Line Geocoding Service exposes data through HTTP, accepting and returing data in JSON format.
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
    "time": "2015-02-18T04:57:56Z"
}
```

**2. Single Point Geocode**

`POST /census/addrfeat`

Payload: JSON object with the following structure:

* number: House number
* streetName: Full name of the street
* zipCode: 5 digit Zip Code
* state: State abbreviation

Example:

```json
{
  "number": 3146,
  "streetName": "M St NW",
  "zipCode": 20007,
  "state": "DC"
}
```

Return: GeoJSON object that includes the point that resulted from geocoding, as well as other attributes including the address that was found.

```json
[
    {
        "type": "Feature",
        "geometry": {
            "type": "Point",
            "coordinates": [
                -77.06204609363698,
                38.90508501171226,
                0
            ]
        },
        "properties": {
            "RFROMHN": "3101",
            "RTOHN": "3199",
            "ZIPL": "20007",
            "FULLNAME": "M St NW",
            "LFROMHN": "3100",
            "LTOHN": "3198",
            "ZIPR": "20007",
            "STATE": "DC"
        }
    }
]
```
