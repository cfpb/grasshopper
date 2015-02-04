package grasshopper.elasticsearch

import grasshopper.feature.Feature
import grasshopper.geojson.FeatureJsonProtocol._
import org.elasticsearch.client.Client
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.index.query.QueryBuilders
import spray.json._

trait Geocode {

  def geocodePoint(client: Client, index: String, indexType: String, address: String): Option[Feature] = {
    val response = client.prepareSearch(index)
      .setTypes(indexType)
      .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
      .setQuery(QueryBuilders.matchPhraseQuery("ADDRESS", address))
      .execute
      .actionGet

    val hits = response.getHits().getHits
    hits.size match {
      case 0 => None
      case _ =>
        val str = hits.map(hit => hit.getSourceAsString).take(1).mkString
        val feature = str.parseJson.convertTo[Feature]
        Some(feature)

    }
  }

}
