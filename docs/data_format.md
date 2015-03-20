Data_Format
===========

Grasshopper Address Point Input Data Standard (request) Version 0.01 
--------------------------------------------------------------------

Intent
------
This document serves the purpose of establishing a standard file format for incoming data into Grasshopper for Address Point Data.  This input standard is the expected data format for ingest into the Grasshopper engine.  It is principally established for 2 specific reasons;

* First this is the standard upon which a document (row) is expected to be developed in Grasshopper.  With this expectation, the application has/will have tools to ingest, process and search against this document (row) format.
* Second, in order to speed efficiency, it would be very helpful if entities with Address Point data who would like Grasshopper to serve their Address Point data, transformed their data into this format.  

Our intent is to provide a search utility for Address Point data which is authoritative and crazy fast.  Help us help you by providing data in this format.

File Naming
-----------
Submitting entities should file data with the following file naming conventions;
If a State:
  * `<FIPS Code>_addresspoints_<yyyymmdd>.<shp|shx|dbf|prj>` where;
    * `<FIPS Code>` is the two digit State FIPS code
    * `<yyyymmdd>` is the four digit year, two digit month and two digit day of the publication date (as good of date) for the source data
    * `<shp|shx|dbf|prj>` are the file suffixes for the four minimum required files for a complete shapefile
    * e.g `06_addresspoints_20150101.shp` for California

If a County:
  * `<FIPS Code>_addresspoints_<yyyymmdd>.<shp|shx|dbf|prj>` where;
    * `<FIPS Code>` is the five digit State FIPS code
    * `<yyyymmdd>` is the four digit year, two digit month and two digit day of the publication date (as good of date) for the source data
    * `<shp|shx|dbf|prj>` are the file suffixes for the four minimum required files for a complete shapefile
    * e.g `06076_addresspoints_20150101.shp` for Sacramento California

File Format
-----------
Currently files will be accepted in shapefile format.  Single files larger than the 2 Gig limit can be broken into counties.

Field Format
------------
|Num|Field|Type|Length|Description|
|---|-----|----|------|-----------|
|1|Id|Long||Unique number, automatically assigned (primary key)|
|2|Shape|Geometry||Geometry column (Shapefile format)|
|3|Number|Varchar|50|House number. Includes possibles prefix or suffix.|
|4|Address|Varchar|255|Complete street address. Includes secondary address unit designators (apt., building, unit, etc)|
|5|Alt_Addr|Varchar|255|Alternate address, if appropriate|
|6|City|Varchar|255|City name|
|7|State|Character|2|Two digit state postal abbreviation code|
|8|ZIP|Varchar|10|maximum length 10 digit postal code of ZIP 5 + ZIP 4 (e.g. 95616-2001)|
|9|Date|Varchar|255|Timestamp for the record, in ISO 8601 format|


e.g. `101 Main St Apt # 101 Springfield VA 22162`

Where:
 ```
  * 101 --> NUMBER
  * Main St Apt # 101 --> ADDRESS
  * Springfield --> CITY
  * VA --> STATE
  * 22162-1001 --> ZIP
```

Projection
----------
Projection must be defined. Data in Global Coordinate System, North American Datum of 1983 (GCS NAD83). Include a .prj file with each shapefile, should have the following contents:

```
  GEOGCS["GCS_North_American_1983",
  DATUM["D_North_American_1983",
  SPHEROID["GRS_1980",6378137,298.257222101]],
  PRIMEM["Greenwich",0],
  UNIT["Degree",0.017453292519943295]]
```

Metadata
--------
Metadata should be defined in any acceptable machine readable format.  Metadata must include descriptors for the a minimum of the following fields;
* source agency
* publication date
* source url (e.g. download location)
* terms and conditions (if any)
* any other field described by the publishing agency 

