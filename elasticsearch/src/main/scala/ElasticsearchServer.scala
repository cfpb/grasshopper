package grasshopper.elasticsearch

import java.io.File
import java.nio.file.Files
import org.elasticsearch.client.Client
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.node.NodeBuilder._

class ElasticsearchServer {

  private val clusterName = "elasticsearch"
  private val dataDir = Files.createTempDirectory("elasticsearch_data_").toFile
  private val settings = ImmutableSettings.settingsBuilder
    .put("path.data", dataDir.toString)
    .put("cluster.name", clusterName)
    .build

  private lazy val node = nodeBuilder().local(true).settings(settings).build

  def client: Client = node.client

  def start(): Unit = {
    node.start()
  }

  def stop(): Unit = {
    node.close()

    try {
      delete(dataDir.getAbsolutePath)
    } catch {
      case e: Exception => // dataDir cleanup failed
    }

  }

  def createAndWaitForIndex(index: String): Unit = {
    client.admin.indices.prepareCreate(index).execute.actionGet()
    client.admin.cluster.prepareHealth(index).setWaitForActiveShards(1).execute.actionGet()
  }

  private def delete(path: String) = {
    def loop(f: File): Seq[File] = {
      f.listFiles.filter(_.isDirectory).flatMap(loop) ++ f.listFiles
    }
    loop(new File(path)).foreach { f =>
      if (!f.delete())
        throw new RuntimeException(s"Failed to delete ${f.getAbsolutePath}")
    }
  }

}
