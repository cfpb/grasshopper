# Census

Service that geocodes a single address string into a point. 


## Building

From the grasshopper root directory, run `sbt` and then `project census`. This will set the current sbt project to census.
From this prompt run `test` to run the automated unit and integration tests

## Running

For development, the project can be run from the `sbt` by typing `re-start`. This will fork a JVM and start the service, which by default requires an Elasticsearch server running on the same machine. 

The project can also be run on a Docker container with the following commands (assumes boot2docker running on a Mac, change IP as appropriate)

First, run a Docker container with Elasticsearch:

`docker run --name elasticsearch -p 9200:9200 -p 9300:9300 elasticsearch`

To load data to Elasticsearch, see the [loader](https://github.com/cfpb/grasshopper-loader)

From the root project, build the project and package it in a single jar:

```
$ sbt
> test assembly
````

From the census directory, build the docker image:

`docker build --rm -t=hmda/grasshopper-census .`

And then run the service linking to the previous container:

`docker run --rm --name grasshopper-census -e ELASTICSEARCH_HOST=192.168.59.103 -e ELASTICSEARCH_PORT=9300 -p 8082:8082 --link elasticsearch:elasticsearch hmda/grasshopper-census`

The Elasticsearch host and port are configurable, passing them as environment variables to the docker container. If not specified, the defaults are _localhost_ and _9300_, respectively.
