[![Build Status](https://travis-ci.org/cfpb/grasshopper.svg?branch=master)](https://travis-ci.org/cfpb/grasshopper) [![Coverage Status](https://coveralls.io/repos/cfpb/grasshopper/badge.svg?branch=master)](https://coveralls.io/r/cfpb/grasshopper?branch=master)

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
Our goal is to reduce burden for financial institutions who need to report location information.  This project was built in order to establish a federal authoritative function for mortgage market needs.  In particular, the Consumer Financial Protection Bureau has elected to provide a geocoding service for those financial institutions which need to establish location attributes in order to meet regulatory functions for rules like [Qualified Mortgage](link) and [Home Mortgage Disclosure Act](link) rules.  These rules require financial institutions to report data on mortgage activities for these financial institutions, and this service offers an authoritative function to meet this need.

We also noticed a gap in approaches to traditional geocoding and wanted to allow an opportunity for growth in the technology around this area.  Many federal, state and local entities have generic needs for geocoding, which this service may help provide.  Many traditional geocoding services hamper government use with a) inflexible terms and conditions (e.g. share alike clauses), b) proprietary technology requiring continuous licensing and/or c) in-ability to use local more relavent data for the search

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
        [info]       addresspoints
        [info]       census
        [info]       client
        [info]       elasticsearch
        [info]       geocoder
        [info]     * grasshopper

        > project addresspoints
        [info] Set current project to addresspoints (in build file: /path/to/grasshopper/)

1. Start the service

    This will retrieve all necessary dependencies, compile Scala source, and
    start a local server.  It also listens for changes to underlying
    source code, and auto-deploys to local server.

        > ~re-start

1. Confirm service is up by browsing to http://localhost:8081/status.

### Docker

All grasshopper services and apps can be built as [Docker](https://docs.docker.com/) images.
[Docker Compose](https://docs.docker.com/compose/) is also used to simplify local development.

**Note:** Docker is a Linux-only tool.  If you are running on Mac or Windows, you will need
[boot2docker](http://boot2docker.io/) or a similar Docker VM setup.

1. Install necessary dependencies (Mac-specific):

        brew install docker docker-compose boot2docker

1. Checkout all other grasshopper-related repos into the same directory as
   grasshopper.  This currently includes:

    1. [grasshopper-loader](https://github.com/cfpb/grasshopper-loader)
    1. [grasshopper-parser](https://github.com/cfpb/grasshopper-parser)
    1. [grasshopper-ui](https://github.com/cfpb/grasshopper-ui)


1. Build grasshopper Scala artifacts:

        $ cd grasshopper
        $ sbt clean assembly

1. To start a **development** version using only grasshopper, loader, and ui run

        $ docker-compose up -d

    **Note:** The `-d` option is necessary since `grasshopper-loader` is not intended to run as a service, and exits immediately.

    Then run `cd` into the grasshopper-ui directory and run

        $ grunt docker

    This gives you everything you need to start development plus:

    - the ability to make UI changes and refresh the browser to view them.
    - an open port to view Elasticsearch data (9200)
        - http://{{docker-provided-ip}}:9200/census/_search?pretty=true
        - http://{{docker-provided-ip}}:9200/address/_search?pretty=true

    To start **all** Build Docker images for all projects

        $ docker-compose -f docker-compose-full.yml up -d

    **Note:** If using `boot2docker`, the following with get you the  `docker-provided-ip` referenced below:

        $ boot2docker ip

1. The dev setup has the /test/data volume so you can place any necessary data in that directory and then load it without having to rebuild the container.

        $ docker-compose run loader

    And then follow the command-line instructions [from the loader repo](https://github.com/cfpb/grasshopper-loader). For example:

        $ node grasshopper-loader.js -d test/data/{{path/to/your/data}}

1. Browse to: http://{{docker-provided-ip}}:8080/status

    If all goes as expected, you should see a message similar to the following:

    ```json
    {
      "addressPointsStatus": {
        "status": "OK",
        "service": "grasshopper-addresspoints",
        "time": "2015-06-01T22:54:31.670Z",
        "host": "b34fd3314b3b"
      },
      "censusStatus": {
        "status": "OK",
        "service": "grasshopper-census",
        "time": "2015-06-01T22:54:31.652Z",
        "host": "6f479f2a0cc6"
      },
      "parserStatus": {
        "status": "OK",
        "time": "2015-06-01T22:54:31.622532+00:00",
        "upSince": "2015-06-01T22:38:17.859000+00:00",
        "host": "a843db3dbe8e"
      }
    }
    ```

    Other URLs:

    - UI = http://{{docker-provided-ip}}
    - Geocoder = http://{{docker-provided-ip}}:8080/geocode/{{address}}

For more details on running via Docker, see [Docker Compose](https://docs.docker.com/compose/).


## Usage

The API documentation is specified in the docs folder, i.e. [Point API](docs/point_api_spec.md)

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
