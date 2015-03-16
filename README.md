#### CFPB Open Source Project Template Instructions

1. Create a new project.
2. Copy these files into the new project.
3. Update the README, replacing the contents below as prescribed.
4. Add any libraries, assets, or hard dependencies whose source code will be included
   in the project's repository to the _Exceptions_ section in the [TERMS](TERMS.md).
  - If no exceptions are needed, remove that section from TERMS.
5. If working with an existing code base, answer the questions on the [open source checklist](opensource-checklist.md) 
6. Delete these instructions and everything up to the _Project Title_ from the README.
7. Write some great software and tell people about it.

> Keep the README fresh! It's the first thing people see and will make the initial impression.

----

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

Other things to include:

  - **Technology stack**: Indicate the technological nature of the software, including primary programming language(s) and whether the software is intended as standalone or as a module in a framework or other ecosystem.
  - **Status**:  Alpha, Beta, 1.1, etc. It's OK to write a sentence, too. The goal is to let interested people know where this project is at. This is also a good place to link to the [CHANGELOG](CHANGELOG.md).
  - **Links to production or demo instances**
  - Describe what sets this apart from related-projects. Linking to another doc or page is OK if this can't be expressed in a sentence or two.


**Screenshot**: If the software has visual components, place a screenshot after the description; e.g.,

![](https://raw.githubusercontent.com/cfpb/open-source-project-template/master/screenshot.png)


## Dependencies

Describe any dependencies that must be installed for this software to work. 
This includes programming languages, databases or other storage mechanisms, build tools, frameworks, and so forth.
If specific versions of other software are required, or known not to work, call that out.

## Installation

Detailed instructions on how to install, configure, and get the project running.
This should be frequently tested to ensure reliability. Alternatively, a link to
another page is fine, but it's important that this works.

## Configuration

If the software is configurable, describe it in detail, either here or in other documentation to which you link.

## Usage

Show users how to use the software. 
Be specific. 
Use appropriate formatting when showing code snippets.

## How to test the software

If the software includes automated tests, detail how to run those tests.

## Known issues

Document any known significant shortcomings with the software.

## Getting help

Instruct users how to get help with this software; this might include links to an issue tracker, wiki, mailing list, etc.

**Example**

If you have questions, concerns, bug reports, etc, please file an issue in this repository's Issue Tracker.

## Getting involved

This section should detail why people should get involved and describe key areas you are
currently focusing on; e.g., trying to get feedback on features, fixing certain bugs, building
important pieces, etc.

General instructions on _how_ to contribute should be stated with a link to [CONTRIBUTING](CONTRIBUTING.md).


----

## Open source licensing info
1. [TERMS](TERMS.md)
2. [LICENSE](LICENSE)
3. [CFPB Source Code Policy](https://github.com/cfpb/source-code-policy/)


----

## Credits and references

1. Projects that inspired you
2. Related projects
3. Books, papers, talks, or other sources that have meaniginful impact or influence on this project 
