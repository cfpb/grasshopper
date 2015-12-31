package grasshopper.geocoder.api.geocode

trait ParallelismFactor {
  val numCores = Runtime.getRuntime.availableProcessors()
}
