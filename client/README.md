# Client

Library that connects to grasshopper individual services, making it easier to compose client applications on top of them.

## Building

From the grasshopper root directory, run `sbt` and then `project client`. This will set the current sbt project to client.
From this prompt run `test` to compile the project and run the automated unit tests.
In addition to this, you can run integration tests that exercise the library against a real system by issuing `it:test`.
For the integration tests to work, the corresponding services must be running.
The endpoints and ports for these services can be configured in two ways:

* By changing the configuration available at `src/main/resources/application.conf`. By default, services are running on localhost.
* By passing the corresponding environment variables:

  - GRASSHOPPER_ADDRESSPOINTS_HOST and GRASSHOPPER_ADDRESSPOINTS_PORT for addresspoints microservice
  - GRASSHOPPER_PARSER_HOST and GRASSHOPPER_PARSER_PORT for parser microservice
  - GRASSHOPPER_CENSUS_HOST and GRASSHOPPER_CENSUS_PORT for census microservice

## Packaging

From the root project, build the project and package it in a single jar:

```
$ sbt
> test assembly
````