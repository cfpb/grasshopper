package grasshopper.addresspoints.search

import java.net.URLDecoder

import com.typesafe.scalalogging.Logger
import feature._
import io.geojson.FeatureJsonProtocol._
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.SearchHit
import org.slf4j.LoggerFactory
import spray.json._

import scala.util.Try

trait Geocode {

  lazy val log = Logger(LoggerFactory.getLogger("grasshopper-grasshopper.addresspoints"))

  def geocode(client: Client, index: String, indexType: String, address: String, count: Int): Try[Array[Feature]] = {
    val addr = URLDecoder.decode(address, "UTF-8")
    log.debug(s"Search Address: ${addr}")
    Try {
      val hits = searchAddress(client, index, indexType, addr)
      hits
        .map(hit => hit.getSourceAsString)
        .take(count)
        .map { s =>
          log.debug(s)
          s.parseJson.convertTo[Feature]
        }.map { f =>
          f.addOrUpdate("match", SearchUtils.percentMatch(addr, f.get("address").getOrElse("").toString))
        }
    }
  }

  private def searchAddress(client: Client, index: String, indexType: String, address: String): Array[SearchHit] = {
    val qb = QueryBuilders.matchQuery("address", address)
    val response = client.prepareSearch(index)
      .setTypes(indexType)
      .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
      .setQuery(qb)
      .execute
      .actionGet

    response.getHits().getHits
  }
}
