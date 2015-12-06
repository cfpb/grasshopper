package grasshopper.geocoder.api

import akka.actor.{ Props, ActorLogging }
import akka.stream.actor.{ WatermarkRequestStrategy, RequestStrategy, ActorSubscriber }
import akka.stream.actor.ActorSubscriberMessage.{ OnComplete, OnError, OnNext }
import grasshopper.geocoder.model.{ GeocodeStats, GeocodeResponse }
import grasshopper.geocoder.protocol.GrasshopperJsonProtocol
import spray.json._

object GeocodeSubscriber {
  def props: Props = Props(new GeocodeSubscriber)
}

class GeocodeSubscriber extends ActorSubscriber with ActorLogging with GrasshopperJsonProtocol {

  var total: Int = 0
  var totalNotParsed: Int = 0
  var pointFeatureCount: Int = 0
  var censusFeatureCount: Int = 0

  override protected def requestStrategy: RequestStrategy = WatermarkRequestStrategy(50)

  override def receive: Receive = {
    case OnNext(g: GeocodeResponse) =>
      val stats = computeGeocodeStats(g)
      log.info(stats.toJson.toString)
    case OnNext(s: String) =>
      log.info(s"received an input string: ${s}")
    case OnError(err: Exception) =>
      log.error(s"ERROR: ${err.getLocalizedMessage}")
    case OnComplete =>
      log.info("Geocoding Stream completed")
  }

  private def computeGeocodeStats(g: GeocodeResponse): GeocodeStats = {
    total += 1
    val inputAddress = g.input
    val parts = g.parts
    val features = g.features
    val pointFeatures = features.filter(f => f.get("source").getOrElse("") == "state-address-points")
    if (pointFeatures.nonEmpty) {
      pointFeatureCount += 1
    }
    val censusFeatures = features.filter(f => f.get("source").getOrElse("") == "census-tiger")
    if (censusFeatures.nonEmpty) {
      censusFeatureCount += 1
    }

    if (parts.isEmpty) {
      totalNotParsed += 1
    }
    val percentageParsed = (1 - (totalNotParsed.toDouble / total.toDouble)) * 100
    GeocodeStats(total, percentageParsed)
  }
}
