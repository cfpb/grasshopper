package grasshopper.geocoder.search.census

import com.typesafe.scalalogging.Logger
import feature.Feature
import geometry.Point
import grasshopper.geocoder.search.census.SearchUtils._
import grasshopper.model.SearchableAddress
import io.geojson.FeatureJsonProtocol._
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.QueryBuilders
import org.slf4j.LoggerFactory
import spray.json._

trait CensusGeocode {

  lazy val censusLogger = Logger(LoggerFactory.getLogger("grasshopper-census"))

  def geocodeLine(client: Client, index: String, indexType: String, addressInput: SearchableAddress, count: Int): Array[Feature] = {
    val hits = searchAddress(client, index, indexType, addressInput)
    val addressNumber = toInt(addressInput.addressNumber).getOrElse(0)
    if (hits.nonEmpty) {
      hits
        .map(hit => hit.getSourceAsString)
        .take(count)
        .map { s =>
          val line = s.parseJson.convertTo[Feature]

          censusLogger.debug(s"Translated JSON to Feature: $line")

          val addressRange = AddressInterpolator.calculateAddressRange(line, addressNumber)
          AddressInterpolator.interpolate(line, addressRange, addressNumber)
        }
        .map(f => f.addOrUpdate("source", "census-tiger"))
        .map { f =>
          val streetName = f.get("FULLNAME").getOrElse("")
          val city = addressInput.city
          val state = f.get("STATE").getOrElse("")
          val zipCodeR = f.get("ZIPR").getOrElse("")
          f.addOrUpdate("address", s"$addressNumber $streetName $city $state $zipCodeR")
        }
    } else {
      censusLogger.warn(s"No hits for input address: $addressInput")
      Array(Feature(Point(0, 0)))
    }
  }

  private def searchAddress(client: Client, index: String, indexType: String, addressInput: SearchableAddress) = {
    censusLogger.debug(s"Searching census data for '$addressInput'...")

    val number = addressInput.addressNumber.toLowerCase
    val street = addressInput.streetName
    val zipCode = addressInput.zipCode
    val state = addressInput.state

    val stateQuery = QueryBuilders.matchQuery("properties.STATE", state)

    //FIXME: Figure out why matchPhraseQuery no longer works under ES 2.2
    //val streetQuery = QueryBuilders.matchPhraseQuery("properties.FULLNAME", street)
    val streetQuery = QueryBuilders.matchQuery("properties.FULLNAME", street)

    val zipLeftQuery = QueryBuilders.termQuery("properties.ZIPL", zipCode)
    val zipRightQuery = QueryBuilders.termQuery("properties.ZIPR", zipCode)

    val zipQuery = QueryBuilders.boolQuery()
      .should(zipLeftQuery)
      .should(zipRightQuery)

    val rightHouseQuery = QueryBuilders.boolQuery()
      .must(QueryBuilders.rangeQuery("properties.RFROMHN").lte(number))
      .must(QueryBuilders.rangeQuery("properties.RTOHN").gte(number))

    val leftHouseQuery = QueryBuilders.boolQuery()
      .must(QueryBuilders.rangeQuery("properties.LFROMHN").lte(number))
      .must(QueryBuilders.rangeQuery("properties.LTOHN").gte(number))

    val houseQuery = QueryBuilders.boolQuery()
      .should(rightHouseQuery)
      .should(leftHouseQuery)

    val filter = QueryBuilders.boolQuery()
      //FIXME: The current house number range queries do not work.  This seems to be due to:
      //       1. the ES 2.x upgrade.
      //       2. the assumption that FROM values are always greater than TO values.
      //       3. TO/FROM fields must be a numeric type for range queries to function properly.
      //.must(houseQuery)
      .must(zipQuery)

    val query = QueryBuilders.boolQuery()
      .must(stateQuery)
      .must(streetQuery)
      .filter(filter)

    censusLogger.debug(s"Elasticsearch query: $query")

    val response = client.prepareSearch(index)
      .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
      .setQuery(query)
      .execute
      .actionGet()

    val hits = response.getHits.getHits

    censusLogger.debug(s"$hits hits from census data for '$addressInput'")

    hits
  }

}
