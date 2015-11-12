package grasshopper.geocoder.search.addresspoints

import com.typesafe.scalalogging.Logger
import feature._
import geometry.Point
import grasshopper.client.parser.model.ParsedAddress
import org.slf4j.LoggerFactory
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.SearchHit
import spray.json._
import io.geojson.FeatureJsonProtocol._

trait AddressPointsGeocode {

  lazy val pointLogger = Logger(LoggerFactory.getLogger("grasshopper-grasshopper.addresspoints"))

  def geocodePoint(client: Client, index: String, indexType: String, address: String, count: Int): Array[Feature] = {
    pointLogger.debug(s"Search Address: ${address}")
    val hits = searchAddress(client, index, indexType, address)
    if (hits.length >= 1) {
      hits
        .map(hit => hit.getSourceAsString)
        .take(count)
        .map(s => s.parseJson.convertTo[Feature])
        .map(f => f.addOrUpdate("source", "state-address-points"))
    } else {
      Array(Feature(Point(0, 0)))
    }
  }

  def geocodePointFields(client: Client, index: String, indexType: String, parsedAddress: ParsedAddress, count: Int): Array[Feature] = {
    val number = parsedAddress.parts.addressNumber
    val street = parsedAddress.parts.streetName
    val city = parsedAddress.parts.city
    val state = parsedAddress.parts.state
    val zip = parsedAddress.parts.zip
    val hits = searchAddressFields(client, index, indexType, number, street, city, state, zip)
    if (hits.length >= 1) {
      hits.map(hit => hit.getSourceAsString)
        .take(count)
        .map(s => s.parseJson.convertTo[Feature])
        .map(f => f.addOrUpdate("source", "state-address-points"))
    } else {
      Array(Feature(Point(0, 0)))
    }
  }

  private def searchAddress(client: Client, index: String, indexType: String, address: String): Array[SearchHit] = {
    val qb = QueryBuilders.matchPhraseQuery("address", address)
    val response = client.prepareSearch(index)
      .setTypes(indexType)
      .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
      .setQuery(qb)
      .execute
      .actionGet

    response.getHits().getHits
  }

  private def searchAddressFields(client: Client, index: String, indexType: String, number: String, streetName: String, city: String, state: String, zipCode: String): Array[SearchHit] = {
    val numberQuery = QueryBuilders.matchQuery("number", number)
    val streetQuery = QueryBuilders.matchPhraseQuery("streetname", streetName)
    val cityQuery = QueryBuilders.matchQuery("city", city)
    val stateQuery = QueryBuilders.matchQuery("state", state)
    val zipQuery = QueryBuilders.matchQuery("zipcode", zipCode)

    val query = QueryBuilders
      .boolQuery()
      .must(numberQuery)
      .must(streetQuery)
      .must(cityQuery)
      .must(stateQuery)
      .must(zipQuery)

    pointLogger.debug(query.toString)

    val response = client.prepareSearch(index)
      .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
      .setQuery(query)
      .execute
      .actionGet()

    response.getHits.getHits

  }
}

