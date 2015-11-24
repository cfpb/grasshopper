package grasshopper.geocoder.api

trait ParallelismFactor {
  val numCores = Runtime.getRuntime.availableProcessors()
}
