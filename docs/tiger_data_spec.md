# ElasticSearch Address TIGER Data Schema

## Description

Data is stored in Elasticsearch, by convention in an index called `census` with type `addrfeat`.
Data format is GeoJSON objects with the following structure:

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
     "ZIPR": "",
     "STATE": "AR"
  }
}
```

A typical search will return records in the following format when using ElasticSearch's REST API:

```json
{
  "_index": "census",
  "_type": "addrfeat",
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
       "ZIPR": "",
       "STATE": "AR"
    }
  }

}
```

## Data creation

The census geocoder uses Elasticsearch synonyms to resolve abbreviations (i.e. St for Street, or MD for Maryland).
The synonyms.txt file with the synonyms definition must be installed in every node in the cluster, in the same directory as the elasticsearch.yml configuration file.

* First, create the index, with the synonyms analyzer settings, and apply that analyzer to the corresponding fields:

```
curl -XPUT 'http://127.0.0.1:9200/census/?pretty=1'  -d '
{
   "mappings" : {
      "census" : {
         "properties" : {
            "properties.FULLNAME" : {
               "type" : "string",
               "analyzer" : "synonyms"
            },
            "properties.STATE": {
               "type": "string",
               "analyzer" : "synonyms"
            }
         }
      }
   },
   "settings" : {
      "analysis" : {
         "filter" : {
            "syns_filter" : {
               "type" : "synonym",
               "synonyms_path" : "synonyms.txt"
            }
         },
         "analyzer" : {
            "synonyms" : {
               "filter" : [
                  "standard",
                  "lowercase",
                  "syns_filter"
               ],
               "type" : "custom",
               "tokenizer" : "standard"
            }
         }
      }
   }
}
'
```

* Check that the mapping is correct:

`curl -XGET 'http://127.0.0.1:9200/census/_mapping?pretty=1'`

```json
{
  "census" : {
    "mappings" : {
      "census" : {
        "properties" : {
          "properties.FULLNAME" : {
            "type" : "string",
            "analyzer" : "synonyms"
          }
        }
      }
    }
  }
}
```

* Use the analyze API to test the synonyms analizer:

`curl -XGET 'http://127.0.0.1:9200/census/_analyze?pretty=1&text=court&analyzer=synonyms'`

```json
{
  "tokens" : [ {
    "token" : "court",
    "start_offset" : 0,
    "end_offset" : 5,
    "type" : "SYNONYM",
    "position" : 1
  }, {
    "token" : "ct",
    "start_offset" : 0,
    "end_offset" : 5,
    "type" : "SYNONYM",
    "position" : 1
  } ]
}
```

At this point, TIGER data can be loaded using the [loader](https://github.com/cfpb/grasshopper-loader)

* Test query (assumes "Main St" is in the census index):

```
curl -XGET 'http://127.0.0.1:9200/census/_search?pretty=1'  -d '
{
    "query": {
        "match_phrase": {
           "properties.FULLNAME": {
               "query": "Main Street"
           }
        }
    }
}
'
```
