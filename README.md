[![Build Status](https://travis-ci.org/cfpb/grasshopper.svg?branch=master)](https://travis-ci.org/cfpb/grasshopper) [![Coverage Status](https://coveralls.io/repos/cfpb/grasshopper/badge.svg?branch=master)](https://coveralls.io/r/cfpb/grasshopper?branch=master)

# Grasshopper

Faster than you can snatch the pebble from our hand, we will return a location.

What Problem This Solves
------------------------
This repo solves the problem of finding a location for geographic text, in particular postal address input. Often called geocoding, this project returns a latitude and longitude (y and x) value for entered postal addresses.  


How This Solves The Problem
---------------------------
Using Elasticsearch and a fabric of high value data, this project offers an API built off of microservices.  These services receive entered text, parses that text for postal address attributes, searches authoritative local, state, and national data on those attributes and then returns the best fit answer location for that entered text.  The intent of this project is a high availability, high volume and high use geocoding.  Other projects contain data source/loading functions and user interface functions, and this project is the back end code for the search algorythm and API services.


Why We Wanted to Solve It
-------------------------
Our goal is to reduce burden for financial institutions who need to report location information.  This project was built in order to establish a federal authoritative function for morgtage market needs.  In particular, the Consumer Finance Protection Bureau has has elected to provide a geocoding service for those financial institutions which need to establish location attributes in order to meet regulatory functions for rules like [Qualified Mortgage](link) and [Home Mortgage Disclosure Act](link) rules.  These rules require financial institutions to report data on mortgage activities for these financial institutions, and this service offers an authoritative function to meet this need.

We also noticed a gap in approaches to traditional geocoding and wanted to allow an opportunity for growth in the technology around this area.  Many federal, state and local entities have generic needs for geocoding, which this service may help provide.  Many traditional geocoding services hamper government use with a) inflexible terms and conditions (e.g. share alike clauses), b) proprietary technology requiring continuous licensing and/or c) in-ability to use local more relavent data for the searching

We encourage forking, adding to the code base and/or general use of the service.  


## Dependencies

### Java 8 JDK
Grasshopper's service layer runs on the Java Virtual Machine (JVM), and requires the Java 8 JDK to build the project.
This project is currenly being built and tested on [Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
See [Oracle's JDK Install Overview](http://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 
for install instructions.

Grasshopper _should_ also run on OpenJDK 8, but has not been tested.

### Scala
Grasshopper's service layer is written in [Scala](http://www.scala-lang.org/).  You will need to 
[download](http://www.scala-lang.org/download/) and [install]() Scala 2.11.x

In addition, you'll need Scala's interactive build tool [sbt](http://www.scala-sbt.org/0.13/tutorial/index.html).
Please refer to the [installation instructions](http://www.scala-sbt.org/0.13/tutorial/Setup.html) to get going.

### Elasticsearch
Grasshopper uses [Elasticsearch](http://www.elasticsearch.org/) as a backend to store data for geocoding.
For dev and test purposes, grasshopper includes an in-memory
[ElasticsearchServer](https://github.com/cfpb/grasshopper/blob/master/elasticsearch/src/main/scala/ElasticsearchServer.scala).
For non-dev environments, you'll want a dedicated Elasticsearch instance.


## Building
Grasshopper uses [sbt's multi-project builds](http://www.scala-sbt.org/0.13/tutorial/Multi-Project.html), 
each project representing a specific task and usually a [Microservice](http://en.wikipedia.org/wiki/Microservices).

```
$ sbt
> ~re-start
```

This will fork a JVM and start the geocoding service. Currently the addresspoints project will expose 
a REST API to resolve addresses to locations in GeoJSON.

## Usage

The API documentation is specified in the docs folder, i.e. [Point API](docs/point_api_spec.md)

## Testing 

To run the tests, from the project directory: 

```
$ sbt
> test
```

This will run unit and integration tests. The integration tests will stand up a temporary Elasticsearch node, no additional dependencies are needed.  

## Known issues

The tests will occasionally print out a stack trace, the in memory Elasticsearch node doesn't load all libraries. So far this is not an issue for the purposes of testing.


## Getting involved

For details on how to get involved, please first read our [CONTRIBUTING](CONTRIBUTING.md) guidelines.


## Open source licensing info
1. [TERMS](TERMS.md)
2. [LICENSE](LICENSE)
3. [CFPB Source Code Policy](https://github.com/cfpb/source-code-policy/)
