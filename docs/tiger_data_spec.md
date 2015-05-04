# ElasticSearch Address TIGER Data Schema

Data is stored in Elasticsearch, by convention in an index called `addrfeat` with type `tiger`.Data format is GeoJSON objects with the following structure:

* type: "Feature"
* geometry: GeoJSON geometry representation
* properties: Fields that include address information from the TIGER ADDRFEAT data set, in particular:
	* FULLNAME: Street name
	* LFROMHN: Start house number, left side
	* LTOHN: End house number, left side
	* RFROMHN: Start house number, right side
	* RTOHN: End house number, right side
	* ZIPL: Zip code, left side
	* ZIPR: Zip code, right side

Example:

```json
{
  "type": "Feature",
  "geometry": {
    "type": "LineString",
    "coordinates": [
       [
         -91.33126,
          34.29961999999999
       ],
       [
         -91.33137999999998,
         34.29961999999999
       ],
       [
         -91.33173999999998,
          34.29961999999999
       ],
       [
         -91.33185999999999,
          34.29961999999999
       ]
     ]
  },
  "properties": {
     "ARIDL": "4003951555205",
     "ARIDR": "",
     "EDGE_MTFCC": "S1400",
     "FULLNAME": "Roth Prairie Rd",
     "LFROMHN": "101",
     "LFROMTYP": "",
     "LINEARID": "110378466099",
     "LTOHN": "103",
     "LTOTYP": "",
     "OFFSETL": "N",
     "OFFSETR": "N",
     "PARITYL": "O",
     "PARITYR": "",
     "PLUS4L": "",
     "PLUS4R": "",
     "RFROMHN": "",
     "RFROMTYP": "",
     "ROAD_MTFCC": "S1400",
     "RTOHN": "",
     "RTOTYP": "",
     "TFIDL": "208595926",
     "TFIDR": "208595636",
     "TLID": "11961970",
     "ZIPL": "72160",
     "ZIPR": ""
  }
}
```

A typical search will return records in the following format when using ElasticSearch's REST API:

```json
{
  "_index": "addrfeat",
  "_type": "tiger",
  "_id": "AU0fPkzMfUUh7D1HEp9O",
  "_score": 1,
  "_source": {
  "type": "Feature",
    "geometry": {
      "type": "LineString",
      "coordinates": [
         [
           -91.33126,
            34.29961999999999
         ],
         [
           -91.33137999999998,
           34.29961999999999
         ],
         [
           -91.33173999999998,
            34.29961999999999
         ],
         [
           -91.33185999999999,
            34.29961999999999
         ]
       ]
    },
    "properties": {
       "ARIDL": "4003951555205",
       "ARIDR": "",
       "EDGE_MTFCC": "S1400",
       "FULLNAME": "Roth Prairie Rd",
       "LFROMHN": "101",
       "LFROMTYP": "",
       "LINEARID": "110378466099",
       "LTOHN": "103",
       "LTOTYP": "",
       "OFFSETL": "N",
       "OFFSETR": "N",
       "PARITYL": "O",
       "PARITYR": "",
       "PLUS4L": "",
       "PLUS4R": "",
       "RFROMHN": "",
       "RFROMTYP": "",
       "ROAD_MTFCC": "S1400",
       "RTOHN": "",
       "RTOTYP": "",
       "TFIDL": "208595926",
       "TFIDR": "208595636",
       "TLID": "11961970",
       "ZIPL": "72160",
       "ZIPR": ""
    }
  }

}
```
