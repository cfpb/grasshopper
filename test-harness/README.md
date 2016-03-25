# Grasshopper Test Harness

## Introduction

This project serves as a testing tool for the Grasshopper project. It is not intended to be deployed to production with
the main software, but to provide some assistance in checking the quality of the Geocoder. 
The test harness provides a few programs that automate geocoding at a larger scale through batching, and provide results
for further analysis.

## `GeocoderTest` - Batch Geocode Testing

The primary function of the test-harness subproject is testing known "good" geocodes against the results of 
the grasshopper geocoder.  This is performed via [`GeocoderTest.scala`](https://github.com/cfpb/grasshopper/blob/master/test-harness/src/main/scala/grasshopper/test/GeocoderTest.scala).

### Setup

`GeocoderTest` depends on several external services.  The full setup is semi-complex, so we use Docker Compose to glue it all together.
The one exception is Elasticsearch (ES). Due to VirtualBox's slow I/O, it is much faster to run ES natively. The below assumes an install
on a Mac.  Adjust as necessary per your environment.

#### Install Elasticsearch

1. Install Elasticsearch 2.2 via Homebrew

    ```
    brew install elasticsearch
    ```
    
    **Warning:** Elasticsearch 1.x is now a separate install from 2.x in Homebrew.  If you have an existing 1.7 install,
    you may need to do some extra cleanup before reinstalling.

2. Configure Elasticsearch

    ```
    cd ~/homebrew/etc/elasticsearch/

    # Backup configs
    mv elasticsearch.yml elasticsearch.yml-orig
    mv logging.yml logging.yml-orig

    # Copy in new configs
    cp <projects-root>/grasshopper/test-harness/conf/elasticsearch/* .
    ```

3. Increase Elasticsearch memory
    ES defaults allocating 1 GB of memory for itself at startup.  For optimal performance, its recommended to use 
    half your system's memory, which can be set via the `ES_HEAP_SIZE` envvar set the memory allocated to ES.  

    ```
    export ES_HEAP_SIZE=4g
    ```

    If you'd like to make this a little more permanent, add this to your shell's profile.  The following should
    work if you're using bash.

    ```
    echo '\n#Elasticsearch memory settings\nexport ES_HEAP_SIZE=4g' >> ~/.bash_profile
    source ~/.bash_profile
    ```

4. Start Elasticsearch

    ```
    elasticsearch
    ```

#### Get all necessary projects

The following projects must be checked out into the same directory:
* `grasshopper`
* `grasshopper-loader`
* `grasshopper-parser`
* `grasshopper-qa`
* `hmda-geo`

### Building

#### Scala projects

All Scala-based project must first be "assembled" prior to building their corresponding Docker images.
The following commands should be executed from the root of the grasshopper project.

* `geocoder` REST API (Default sbt project)

    ```
    sbt 'clean' 'assembly'
    ```

* `test_harness`

    ```
    sbt 'project test_harness' 'clean' 'assembly'
    ```

#### Docker images

Docker Compose does not always recognize changes, especially for the Scala-based projects, so
you must _force_ it to do a full rebuild each time you change the underlying code via `build --no-cache`.
Be careful with this, though, as you probably **don't** want to rebuild the hmda-geo images every time, 
which are large, slow to build, and change infrequently.

##### First build
This will build **all** projects from scratch.  This will take a while as it has to build the 
hmda-geo images, which include loading a large number of shape files into a PostGIS database.

```
docker-compose -f docker-compose-test-harness.yaml build --no-cache
```

### Run test-harness via Docker Compose

All Docker Compose files are in the root of the `grasshopper` project.  When using the 
`test-harness` you must explicitly set its config file with  `-f docker-compose-test-harness.yaml`.

#### Upload geo data with grasshopper-loader

1. State address points data

    ```
    docker-compose -f docker-compose-test-harness.yaml run loader ./index.js -f data.json -h elasticsearch -c 1
    ```

1. Census TIGER data

    ```
    docker-compose -f docker-compose-test-harness.yaml run loader ./tiger.js -h elasticsearch -c 1
    ```

**Note:** There _seems_ to be bug in the current loader implementation that can cause I/O errors when
processing files concurrently.  The below examples override the default concurrency setting (`-c 1`) to 
make sure only a single file is processed at a time.  This does make the loading process considerably
slower, but reduces the chances of failures and the need for multiple runs of the loader.

#### Run test-harness

1. Delete previous run's `out.csv` file.  If the file is still in place, test-harness will _seem_ to
be working, but actually hangs without error.  Running this from the root of the grasshopper project
would look like:

    ```
    rm -f ../grasshopper-qa/data/out.csv
    ```

1. Run the `test_harness` service.

    ```
    docker-compose -f docker-compose-test-harness.yaml run --rm test_harness
    ```

    **Note:** `test_harness` does not exit cleanly when it's done.  You'll see a "DONE" message
    on the screen, but you'll have to `<ctrl> + c` to get it to stop the container.

### Making changes

Once you have it all working, of course you'll want to change...something.

#### Rebuilding projects

The following patterns work well, and guarantees you get a full rebuild, 
with no leftovers from previous sbt or Docker builds.

If you are only making small changes to a Scala project, omitting the `clean` step
will save you several minutes of build time.  However, if you're switching branches, or merging in the
latest changes, you should **always** do a clean or you risk not getting all changes.

The examples below include an `sbt` step.  This should obviously be omitted for non-Scala projects.

##### Long-running services

Several services (`geocoder`, `parser`, `hmda_geo_api`, `hmda_geo_postgis`) are intended to remain running.
The following is an example of rebuilding and deploying the geocoder service.

```
docker-compose -f docker-compose-test-harness.yaml stop geocoder && \
docker-compose -f docker-compose-test-harness.yaml rm -vf geocoder && \
sbt 'clean' 'assembly' && \
docker-compose -f docker-compose-test-harness.yaml build --no-cache geocoder && \
docker-compose -f docker-compose-test-harness.yaml up -d geocoder
```

##### Run-and-done services

The `loader` and `test_harness` are meant to be run as one-time jobs. The following is an
example of rebuilding and running `test_harness`.

```
sbt 'project test_harness' 'clean' 'assembly' && \
docker-compose -f docker-compose-test-harness.yaml build --no-cache test_harness && \
docker-compose -f docker-compose-test-harness.yaml run --rm test_harness
```

#### Wiping Elasticsearch data

Sometimes you want to wipe all or some portion of the data, and reload it.

```
curl -X DELETE http://localhost:9200/_all
curl -X DELETE http://localhost:9200/address
curl -X DELETE http://localhost:9200/census
```

## Other Tools

### ExtractIndex

This program extracts the contents of an index to a file. It is useful for extracting indexes that contain
state level data from Elasticsearch, dumping them to a file that can be used for further processing. 
It requires the [hmda-geo](https://github.com/cfpb/hmda-geo) project to be running in order to add the Census Tract information
to the original address points. 

It can be run as follows from an `sbt` promt:

```shell
> project test_harness
> set mainClass in (Compile, run) := Some("grasshopper.test.ExtractIndex")
> run <index> <type>
```

The last command requires two parameters, `<index>` and `<type>` representing the corresponding index and type in Elasticsearch
The output from running this will be saved in the `test-harness/target` folder with the name `<index>-<type>`.csv
This file has the following structure:

`Input Address, Input Longitude,Input Latitude, Input Census Tract ID`


### TractOverlay

This program takes the output from the previous program. It will require both the `hmda-geo` project mentioned
above as well as the [Grasshopper Parser](https://github.com/cfpb/grasshopper-parser) running in order to produce results. 

It can be run as follows from an `sbt` prompt: 

```shell
> project test_harness
> set mainClass in (Compile, run) := Some("grasshopper.test.TractOverlay")
> run <path>
```

The last command requires one parameter, `<path>`, which is the absolute path to the file generated by `ExtractIndex`
The results are saved in the test-harness/target directory in a file called `census-results.csv`.
This file contains the following structure:

`Input Address, Input Longitude,Input Latitude, Input Census Tract ID, Census Longitude, Census Latitude, Distance, Output Census Tract ID` 

`Distance` is the distance in Km between the input point and the geocoded point.
