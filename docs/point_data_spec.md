# ElasticSearch Address Point Data Schema

Data is stored in Elasticsearch, by convention in an index called `address` with type `point`.Data format is GeoJSON objects with the following structure:

* type: "Feature"
* geometry: GeoJSON geometry representation
* properties: Fields that include address information, in particular:
	* address: Full address text
	* alt_address: Full alternate address text (if available)
	* load_date: Date for record at origin (if available). ISO 8601 format

Example:

```json
{
  "type": "Feature",
	"properties": {
    "address": "11175 N AR 59 Hwy Gravette 72736 AR",
    "alt_address": "",
    "load_date": "2015-02-19T10:28:00-05:00"
	},
	"geometry": {
      "type": "Point",
      "coordinates": [
        -94.44219850463328,
         36.38888678213173
      ]
    }
}
```

A typical search will return records in the following format when using ElasticSearch's REST API:

```json
{
  "_index": "address",
  "_type": "point",
  "_id": "AUs8pqTyEWGWBxx_H2sW",
  "_score": 1,
  "_source": {
  "type": "Feature",
    "properties": {
      "address": "11175 N AR 59 Hwy Gravette 72736 AR",
      "load_date": "2015-02-19T10:28:00-05:00"
    },
    "geometry": {
      "type": "Point",
      "coordinates": [
        -94.44219850463328,
         36.38888678213173
      ]
    }
  }
}
```
