package grasshopper.census.search

import com.typesafe.scalalogging.Logger
import feature.Feature
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.{ QueryBuilders, FilterBuilders }
import org.elasticsearch.search.SearchHit
import org.slf4j.LoggerFactory
import grasshopper.census.model.ParsedInputAddress
import io.geojson.FeatureJsonProtocol._
import spray.json._

import scala.util.Try

trait CensusGeocode {

  lazy val log = Logger(LoggerFactory.getLogger("grasshopper-census"))

  def geocodeLine(client: Client, index: String, indexType: String, addressInput: ParsedInputAddress, count: Int): Try[Array[Feature]] = {
    log.debug(s"Search Address: ${addressInput.toString()}")
    Try {
      val hits = searchAddress(client, index, indexType, addressInput)
      val addressNumber = addressInput.number
      hits
        .map(hit => hit.getSourceAsString)
        .take(count)
        .map { s =>
          log.info(s)
          val line = s.parseJson.convertTo[Feature]
          val addressRange = AddressInterpolator.calculateAddressRange(line, addressNumber)
          AddressInterpolator.interpolate(line, addressRange, addressNumber)
        }
    }
  }

  private def searchAddress(client: Client, index: String, indexType: String, addressInput: ParsedInputAddress) = {
    val number = addressInput.number
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

    log.debug(query.toString)

    val response = client.prepareSearch(index)
      .setTypes(indexType)
      .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
      .setQuery(query)
      .execute
      .actionGet()

    response.getHits.getHits

  }

}
