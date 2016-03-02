package grasshopper.elasticsearch

import java.io.File
import java.nio.file.Files
import com.typesafe.scalalogging.Logger
import org.elasticsearch.client.Client
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.node.Node
import org.elasticsearch.node.NodeBuilder._
import feature._
import io.geojson.FeatureJsonProtocol._
import org.slf4j.LoggerFactory
import spray.json._

class ElasticsearchServer {

  private val clusterName = "elasticsearch"
  private val dataDir = Files.createTempDirectory("elasticsearch_data_").toFile
  private val settings = Settings.settingsBuilder
    .put("path.home", dataDir.toString)
    .put("path.data", dataDir.toString)
    .put("cluster.name", clusterName)
    .build

  lazy val log = Logger(LoggerFactory.getLogger("grasshopper-elasticsearchserver"))

  private lazy val node = nodeBuilder().local(true).settings(settings).build

  def client: Client = node.client

  def start(): Node = {
    node.start()
  }

  def stop(): Unit = {
    client.close()
    node.close()

    try {
      delete(dataDir.getAbsolutePath)
    } catch {
      case e: Exception => // dataDir cleanup failed
    }

  }

  def createAndWaitForIndex(index: String): Unit = {
    val a = client.admin.indices.prepareCreate(index).execute.actionGet()
    val b = client.admin.cluster.prepareHealth(index).setWaitForActiveShards(1).execute.actionGet()
  }

  def loadFeature(index: String, indexType: String, f: Feature): Boolean = {
    val response = client
      .prepareIndex(index, indexType)
      .setSource(f.toJson.toString)
      .execute()
      .actionGet()
    response.isCreated
  }

  def deleteById(index: String, indexType: String, id: String): Boolean = {
    val response = client
      .prepareDelete(index, indexType, id)
      .execute()
      .actionGet()
    response.isFound
  }

  private def delete(path: String) = {
    def loop(f: File): Seq[File] = {
      f.listFiles.filter(_.isDirectory).flatMap(loop) ++ f.listFiles
    }
    loop(new File(path)).foreach { f =>
      if (!f.delete())
        log.error(s"Failed to delete ${f.getAbsolutePath}")
    }
  }

}
