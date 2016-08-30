[![Build Status](https://travis-ci.org/cfpb/grasshopper.svg?branch=master)](https://travis-ci.org/cfpb/grasshopper) [![codecov.io](https://codecov.io/github/cfpb/grasshopper/coverage.svg?branch=master)](https://codecov.io/github/cfpb/grasshopper?branch=master) 

# Grasshopper

Faster than you can snatch the pebble from our hand, we will return a location.

What Problem This Solves
------------------------
This repo solves the problem of finding a location for geographic text, in particular postal address input. Often called geocoding, this project returns a latitude and longitude (y and x) value for entered postal addresses.


How This Solves The Problem
---------------------------
Using Elasticsearch and a fabric of high value data, this project offers an API built off of microservices.  These services receive entered text, parse that text for postal address attributes, search authoritative local, state, and national data on those attributes and then return the best fit answer location for that entered text.  The intent of this project is a high availability, high volume and high use geocoding service.  Other projects contain data source/loading functions and user interface functions, and this project is the back end code for the search algorithm and API services.


Why We Wanted to Solve It
-------------------------
Our goal is to reduce burden for financial institutions who need to report location information.  This project was built in order to establish a federal authoritative function for mortgage market needs.  In particular, the Consumer Financial Protection Bureau has elected to provide a geocoding service for those financial institutions which need to establish location attributes in order to meet regulatory functions for rules like _Qualified Mortgage_ and _Home Mortgage Disclosure Act_ rules.  These rules require financial institutions to report data on mortgage activities for these financial institutions, and this service offers an authoritative function to meet this need.

We also noticed a gap in approaches to traditional geocoding and wanted to allow an opportunity for growth in the technology around this area.  Many federal, state and local entities have generic needs for geocoding, which this service may help provide.  Many traditional geocoding services hamper government use with a) inflexible terms and conditions (e.g. share alike clauses), b) proprietary technology requiring continuous licensing and/or c) in-ability to use local more relevant data for the search.

We encourage forking, adding to the code base and/or general use of the service.  


## Dependencies

### Java 8 JDK
Grasshopper's service layer runs on the Java Virtual Machine (JVM), and requires the Java 8 JDK to build and run the project.
This project is currenly being built and tested on [Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
See [Oracle's JDK Install Overview](http://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html)
for install instructions.

Grasshopper _should_ also run on OpenJDK 8.

### Scala
Grasshopper's service layer is written in [Scala](http://www.scala-lang.org/).  To build it, you will need to
[download](http://www.scala-lang.org/download/) and [install](http://www.scala-lang.org/download/install.html)
Scala 2.11.x

In addition, you'll need Scala's interactive build tool [sbt](http://www.scala-sbt.org/0.13/tutorial/index.html).
Please refer to the [installation instructions](http://www.scala-sbt.org/0.13/tutorial/Setup.html) to get going.

### Elasticsearch
Grasshopper uses [Elasticsearch](http://www.elasticsearch.org/) as a backend to store data for geocoding.
For dev and test purposes, grasshopper includes an in-memory
[ElasticsearchServer](https://github.com/cfpb/grasshopper/blob/master/elasticsearch/src/main/scala/ElasticsearchServer.scala).
For non-dev environments, you'll want a dedicated Elasticsearch instance.


## Building & Running
Grasshopper uses [sbt's multi-project builds](http://www.scala-sbt.org/0.13/tutorial/Multi-Project.html),
each project representing a specific task and usually a [Microservice](http://en.wikipedia.org/wiki/Microservices).

### Interactive

1. Start `sbt`

        $ sbt

1. Select project to build and run

        > projects
        [info] In file:/Users/keelerh/Projects/grasshopper/
        [info]     client
        [info]     elasticsearch
        [info]     geocoder
        [info]   * grasshopper
        [info]     metrics
        [info]     model

        > project geocoder
        [info] Set current project to geocoder (in build file: /path/to/geocoder/)

1. Start the service

This will retrieve all necessary dependencies, compile Scala source, and start a local server.  It also listens for changes to underlying source code, and auto-deploys to local server.

        > ~re-start

1. Confirm service is up by browsing to http://localhost:31010.

### Docker

All grasshopper services and apps can be built as [Docker](https://docs.docker.com/) images.
[Docker Compose](https://docs.docker.com/compose/) is also used to simplify local development.

#### Docker Setup

Docker is a Linux-only tool.  If you are developing on Mac or Windows, you will need
a VM to run Docker.  Below are the steps for setting up a VirtualBox-based VM using
[Docker Machine](https://docs.docker.com/machine/).

1. Install necessary dependencies (Mac-specific):

        brew install docker docker-compose docker-machine

1. Create a Docker VM using Docker Machine, and point the Docker client to it:

        docker-machine create -d virtualbox docker-vm
        eval "$(docker-machine env docker-vm)"

1. Discover the Docker Host's IP

        docker-machine ip docker-vm

    **Note:** This is referred to as `{{docker-host-ip}}` throughout this doc.

#### Developing with Docker Compose

1. Checkout all other grasshopper-related repos into the same directory as
   grasshopper.  This currently includes:

    1. [grasshopper-loader](https://github.com/cfpb/grasshopper-loader)
    1. [grasshopper-parser](https://github.com/cfpb/grasshopper-parser)
    1. [grasshopper-ui](https://github.com/cfpb/grasshopper-ui)

1. Assemble Scala projects into Java artifacts:

        cd grasshopper
        sbt clean assembly

    **Note:** This is necessary because the `geocoder` Docker image is purely Java,
    so the Scala code must first be compiled and packaged to run in that environment.
    This step must be repeated with each change to the `grasshopper` project.

1. Start all projects

        docker-compose up -d

1. Browse to the web-based containers to confirm they're working:

    | Container       | URL                             |
    |-----------------|---------------------------------|
    | `geocoder`      | http://{{docker-host-ip}}:31010 |
    | `parser`        | http://{{docker-host-ip}}:5000  |
    | `ui`            | http://{{docker-host-ip}}       |
    | `elasticsearch` | http://{{docker-host-ip}}:9200  |


#### Making changes to running containers

The `grasshopper-ui` and `grasshopper-parser` projects support auto-reload of code, so you don't have to rebuild their respective images with each code change.  `grasshopper-ui` even has a Docker-specific Grunt task for further dev-friendliness.  This means you can make UI changes and just refresh the browser to view them.

    cd ../grasshopper-ui
    grunt docker

The default Compose setup also mounts the local `grasshopper-loader/test/data` directory into the `grasshopper-loader` container so you can place files there and load them without having to rebuild.

#### Loading address data

The `grasshopper-loader` project comes with some small test data files.  You can load state address point and Census TIGER line data as follows:

    docker-compose run loader ./index.js -f path/to/data.json
    docker-compose run loader ./tiger.js -d path/to/tiger

For further details on loading data, see [grasshopper-loader](https://github.com/cfpb/grasshopper-loader).


#### Running the production-like full stack

If you'd like to see the "full stack", which adds several logging and monitoring services,
just point `docker-compose` at the "full" setup.  This will start **a lot** of containers,
so no need to run this setup during development.

    docker-compose -f docker-compose-full.yml up -d



## Testing

To run the tests, from the project directory:

```
$ sbt
> test
```

This will run unit and integration tests. The integration tests will stand up a temporary Elasticsearch node, no additional dependencies are needed.

In addition to regular testing, some projects (i.e. client, geocoder) also have integration tests that can be run against a live system.
To run these, first make sure that the underlying dependencies have been deployed and are running (addresspoints, parser and census services).
The underlying services need to have the necessary data to pass the tests.

```
$ sbt
> project geocoder
> it:test
```


## Known issues

The tests will occasionally print out a stack trace, the in memory Elasticsearch node doesn't load all libraries. So far this is not an issue for the purposes of testing.


## Getting involved

For details on how to get involved, please first read our [CONTRIBUTING](CONTRIBUTING.md) guidelines.


## Open source licensing info
1. [TERMS](TERMS.md)
2. [LICENSE](LICENSE)
3. [CFPB Source Code Policy](https://github.com/cfpb/source-code-policy/)
