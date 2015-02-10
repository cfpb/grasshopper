# Addresspoints

Service that geocodes a single address string into a point. 


## Building

From the grasshopper root directory, run `sbt` and then `project addresspoints`. This will set the current sbt project to addresspoints.
From this prompt run `test` to run the automated unit and integration tests. If everything passes, run `docker:publishLocal` to create a Docker image. 
The Dockerfile is created in `target/docker/Dockerfile`

## Running

For development, the project can be run from the `sbt` by typing `re-start`. This will fork a JVM and start the service, which by default requires an Elasticsearch server running on the same machine. 

The project can also be run on a Docker container with the following commands (assumes boot2docker running on a Mac, change IP as appropriate)

First, run a Docker container with Elasticsearch:

`docker run --name elasticsearch elasticsearch`

And then run the service linking to the previous container:

`docker run --name addresspoints -e ELASTICSEARCH_HOST=192.168.59.103 -e ELASTICSEARCH_PORT=9300 -p 8080:8080 --link elasticsearch:elasticsearch addresspoints:<version>`

The Elasticsearch host and port are configurable, passing them as environment variables to the docker container. If not specified, the defaults are _localhost_ and _9300_, respectively.
Version is the current version of the software as specified in `build.sbt`
