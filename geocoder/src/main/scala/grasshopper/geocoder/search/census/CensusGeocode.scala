package grasshopper.geocoder.search.census

import com.typesafe.scalalogging.Logger
import feature.Feature
import geometry.Point
import grasshopper.geocoder.search.census.SearchUtils._
import grasshopper.model.census.ParsedInputAddress
import io.geojson.FeatureJsonProtocol._
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.{ FilterBuilders, QueryBuilders }
import org.slf4j.LoggerFactory
import spray.json._

import scala.util.Try

trait CensusGeocode {

  lazy val censusLogger = Logger(LoggerFactory.getLogger("grasshopper-census"))

  def geocodeLine(client: Client, index: String, indexType: String, addressInput: ParsedInputAddress, count: Int): Try[Array[Feature]] = {
    censusLogger.debug(s"Search Address: ${addressInput.toString()}")
    Try {
      val hits = searchAddress(client, index, indexType, addressInput)
      val addressNumber = toInt(addressInput.addressNumber).getOrElse(0)
      hits
        .map(hit => hit.getSourceAsString)
        .take(count)
        .map { s =>
          val line = s.parseJson.convertTo[Feature]
          censusLogger.debug(line.toJson.toString)
          val addressRange = AddressInterpolator.calculateAddressRange(line, addressNumber)
          AddressInterpolator.interpolate(line, addressRange, addressNumber)
        }
    }
  }

  def geocodeLine1(client: Client, index: String, indexType: String, addressInput: ParsedInputAddress, count: Int): Array[Feature] = {
    censusLogger.debug(s"Search Address: ${addressInput.toString()}")
    val hits = searchAddress(client, index, indexType, addressInput)
    val addressNumber = toInt(addressInput.addressNumber).getOrElse(0)
    if (hits.length >= 1) {
      hits
        .map(hit => hit.getSourceAsString)
        .take(count)
        .map { s =>
          val line = s.parseJson.convertTo[Feature]
          censusLogger.debug(line.toJson.toString)
          val addressRange = AddressInterpolator.calculateAddressRange(line, addressNumber)
          AddressInterpolator.interpolate(line, addressRange, addressNumber)
        }
    } else {
      Array(Feature(Point(0, 0)))
    }
  }

  private def searchAddress(client: Client, index: String, indexType: String, addressInput: ParsedInputAddress) = {
    censusLogger.debug(s"Searching on ${addressInput}")

    val number = addressInput.addressNumber.toLowerCase
    val street = addressInput.streetName
    val zipCode = addressInput.zipCode
    val state = addressInput.state

    val stateQuery = QueryBuilders.matchQuery("STATE", state)

    val streetQuery = QueryBuilders.matchPhraseQuery("FULLNAME", street)

    val zipLeftFilter = FilterBuilders.termFilter("ZIPL", zipCode)
    val zipRightFilter = FilterBuilders.termFilter("ZIPR", zipCode)
    val zipFilter = FilterBuilders.orFilter(zipLeftFilter, zipRightFilter)

    val rightHouseFilter = FilterBuilders.andFilter(
      FilterBuilders.rangeFilter("RFROMHN").lte(number),
      FilterBuilders.rangeFilter("RTOHN").gte(number)
    )

    val leftHouseFilter = FilterBuilders.andFilter(
      FilterBuilders.rangeFilter("LFROMHN").lte(number),
      FilterBuilders.rangeFilter("LTOHN").gte(number)
    )

    val houseFilter = FilterBuilders.orFilter(rightHouseFilter, leftHouseFilter)

    val filter = FilterBuilders.andFilter(houseFilter, zipFilter)

    val boolQuery = QueryBuilders
      .boolQuery()
      .must(stateQuery)
      .must(streetQuery)

    val query = QueryBuilders.filteredQuery(boolQuery, filter)

    censusLogger.debug(query.toString)

    val response = client.prepareSearch(index)
      .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
      .setQuery(query)
      .execute
      .actionGet()

    response.getHits.getHits

  }

}
