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

Grasshopper needs [Elasticsearch](http://www.elasticsearch.org/) as a backend to store data for geocoding. 
The services layer runs on the Java Virtual Machine (JVM). The project is being built and tested on JDK 8, but JDK 7 and OpenJDK versions 7 and 8 should also work. 

Describe any dependencies that must be installed for this software to work. 
This includes programming languages, databases or other storage mechanisms, build tools, frameworks, and so forth.
If specific versions of other software are required, or known not to work, call that out.

## Building

To build the project, `sbt` is required. Please refer to the [installation instructions](http://www.scala-sbt.org/0.13/tutorial/Setup.html) for your platform

Grasshopper is a multi-module sbt project, each project has a specific task and usually represents a [Microservice](http://en.wikipedia.org/wiki/Microservices).

Once installed, from the project directory run the following:

```
$ sbt
> ~re-start
```

This will fork a JVM and start the geocoding service. Currently the addresspoints project will expose a REST API to resolve addresses to locations in GeoJSON.


## How to test the software

To run the tests, from the project directory: 

```
$ sbt
> test
```

This will run unit and integration tests. The integration tests will stand up a temporary Elasticsearch node, no additional dependencies are needed.  

## Known issues

The tests will print out a stack trace, the in memory Elasticsearch node doesn't load all libraries. So far this is not an issue for the purposes of testing.


## Getting involved

For details on how to get involved, please first read our [CONTRIBUTING](CONTRIBUTING.md) guidelines.

----

## Open source licensing info
1. [TERMS](TERMS.md)
2. [LICENSE](LICENSE)
3. [CFPB Source Code Policy](https://github.com/cfpb/source-code-policy/)