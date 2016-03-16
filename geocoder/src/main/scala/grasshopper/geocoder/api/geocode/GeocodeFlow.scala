package grasshopper.geocoder.api.geocode

import akka.stream.FlowShape
import akka.stream.scaladsl._
import akka.NotUsed
import feature._
import geometry.Point
import grasshopper.client.parser.AddressParserClient
import grasshopper.geocoder.api.stats.GeocodeStatsSubscriber
import grasshopper.geocoder.model._
import grasshopper.geocoder.search.addresspoints.AddressPointsGeocode
import grasshopper.geocoder.search.census.CensusGeocode
import grasshopper.client.parser.model.ParsedAddress
import grasshopper.model.SearchableAddress
import org.elasticsearch.client.Client
import scala.concurrent.ExecutionContext

trait GeocodeFlow extends AddressPointsGeocode with CensusGeocode with ParallelismFactor {

  def client: Client

  def parseFlow: Flow[String, ParsedAddress, NotUsed] = {
    Flow[String]
      .mapAsync(numCores)(a => AddressParserClient.parse(a))
      .map { x =>
        if (x.isRight) {
          x.right.getOrElse(ParsedAddress.empty)
        } else {
          ParsedAddress.empty
        }
      }
  }

  def parsedInputAddressFlow: Flow[ParsedAddress, SearchableAddress, NotUsed] = {
    Flow[ParsedAddress]
      .map { parsed =>

        val partMap: Map[String, String] = parsed.parts.map(part => (part.code, part.value)).toMap

        SearchableAddress(
          partMap.getOrElse("address_number_full", ""),
          partMap.getOrElse("street_name_full", ""),
          partMap.getOrElse("city_name", ""),
          partMap.getOrElse("zip_code", ""),
          partMap.getOrElse("state_name", "")
        )
      }
  }

  def geocodePointFlow: Flow[String, Feature, NotUsed] = {
    Flow[String]
      .map(s => geocodePoint(client, "address", "point", s, 1).head)
  }

  def geocodePointFieldsFlow: Flow[SearchableAddress, Feature, NotUsed] = {
    Flow[SearchableAddress]
      .map(p => geocodePointFields(client, "address", "point", p, 1).head)
  }

  def geocodeLineFlow: Flow[SearchableAddress, Feature, NotUsed] = {
    Flow[SearchableAddress]
      .map(p => geocodeLine(client, "census", "addrfeat", p, 1).head)
  }

  def tupleToListFlow[T]: Flow[(T, T), List[T], NotUsed] = {
    Flow[(T, T)]
      .map(t => List(t._1, t._2))
  }

  def generateResponseFlow: Flow[(ParsedAddress, List[Feature]), GeocodeResponse, NotUsed] = {
    Flow[(ParsedAddress, List[Feature])]
      .map(t => GeocodeResponse(t._1.input, t._1.parts, t._2))
  }

  def featureToCsv: Flow[Feature, String, NotUsed] = {
    Flow[Feature]
      .map { f =>
        val address = f.get("address").getOrElse("").toString
        val longitude = f.geometry.centroid.x
        val latitude = f.geometry.centroid.y
        s"${address},${longitude},${latitude}"
      }
  }

  def filterFeatureListFlow: Flow[List[Feature], List[Feature], NotUsed] = {
    Flow[List[Feature]].map(xs => xs.filter(f => f.geometry.centroid.x != 0 && f.geometry.centroid.y != 0))
  }

  def geocodeFlow(implicit ec: ExecutionContext): Flow[String, GeocodeResponse, NotUsed] = {
    Flow.fromGraph(
      GraphDSL.create() { implicit b: GraphDSL.Builder[NotUsed] =>
        import GraphDSL.Implicits._

        val input = b.add(Flow[String])
        val broadcastParsed = b.add(Broadcast[ParsedAddress](2))
        val pFlow = b.add(parseFlow)
        val pInputFlow = b.add(parsedInputAddressFlow)
        val broadcastSearchable = b.add(Broadcast[SearchableAddress](2))
        val point = b.add(geocodePointFieldsFlow)
        val line = b.add(geocodeLineFlow)
        val zip = b.add(Zip[Feature, Feature])
        val features = b.add(tupleToListFlow[Feature].via(filterFeatureListFlow))
        val zip1 = b.add(Zip[ParsedAddress, List[Feature]])
        val response = b.add(generateResponseFlow)
        val responseBroadcast = b.add(Broadcast[GeocodeResponse](2))

        input ~> pFlow ~> broadcastParsed ~> pInputFlow ~> broadcastSearchable
        broadcastSearchable ~> line ~> zip.in0
        broadcastSearchable ~> point ~> zip.in1
        broadcastParsed ~> zip1.in0
        zip.out ~> features ~> zip1.in1
        zip1.out ~> response
        response ~> responseBroadcast ~> Sink.actorSubscriber(GeocodeStatsSubscriber.props)

        FlowShape(input.in, responseBroadcast.outlet)

      }
    )
  }

  def filterPointResultFlow: Flow[GeocodeResponse, Feature, NotUsed] = {

    def predicate(source: String): (Feature) => Boolean = { f =>
      f.get("source")
        .getOrElse("")
        .toString == source
    }

    Flow[GeocodeResponse]
      .map(r => r.features)
      .map { features =>
        val points = features
          .filter(predicate("state-address-points"))
        val lines = features
          .filter(predicate("census-tiger"))

        if (points.nonEmpty) {
          points.head
        } else if (lines.nonEmpty) {
          lines.head
        } else {
          val schema = Schema(List(Field("geom", GeometryType()), Field("address", StringType())))
          val values = Map("geom" -> Point(0, 0), "address" -> "ADDRESS NOT FOUND")
          Feature(schema, values)
        }
      }

  }
}

