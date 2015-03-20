package grasshopper.elasticsearch

import feature._
import io.geojson.FeatureJsonProtocol._
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.QueryBuilders
import spray.json._
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import scala.util.{ Success, Failure, Try }

trait Geocode {

  lazy val log = Logger(LoggerFactory.getLogger("grasshopper-geocode"))

  def geocode(client: Client, index: String, indexType: String, address: String): Try[Feature] = {
    Try {
      val response = client.prepareSearch(index)
        .setTypes(indexType)
        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
        .setQuery(QueryBuilders.matchPhraseQuery("ADDRESS", address))
        .execute
        .actionGet

      val hits = response.getHits().getHits
      if (hits.size > 0) {
        val str = hits.map(hit => hit.getSourceAsString).take(1).mkString
        log.debug(str)
        str.parseJson.convertTo[Feature]
      } else {
        val e = new Exception(s"Address not Found: ${address}")
        log.error(e.getLocalizedMessage)
        throw e
      }
    }
  }
}
