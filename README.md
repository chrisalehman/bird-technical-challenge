# Batch Scheduler Application

This document presents a complete implementation of the Bird coding challenge. The original readme has been moved to README-PROJECT.md.

## Design

The application is written in Kotlin, leverages the Micronaut framework, and uses Gradle for the build system. It is completely self-contained. It runs in one of two modes: as a regular CLI that listens on stdin for input from the terminal, or as a client/server app for automated testing. Note that this it is based on a Function Micronaut profile and so with some additional changes could be deployed as a FaaS.

The backend relies on an in-memory H2 relational database for storing the City, Batch and Deployment data. This could be swapped out with a enterprise-grade datastore were we to need to scale it. 

The main challenge in building the app was managing the time interval rules: 
1. No batch can be deployed to a city if it would exceed a total cap on Birds across all deployments there for any overlapping time period.  
2. No batch can be double-booked with itself for overlapping time periods. As a final twist, no overlaps are allowed between cities taking travel time into account. 

I opted to solve these business rules with an open source Interval Tree implementation available [here](https://github.com/Breinify/brein-time-utilities). The library provides an easy way to populate the tree in memory and search for overlaps in time intervals in O(log(n)) time. The application endeavors to keep the H2 database and the IntervalTrees in sync. If the app were to be expanded to a true distributed system, thread safety and synchronization would need to be more comprehensively addressed.  

## How to build

I provided an env-setup.sh script in $PROJECT_HOME/bin. Assuming a Unix or Mac OS, this will install all required system packages. I also created a Makefile to simplify the gradle commands.

To build: 

```
make build-all
```

To run tests:

```
make test
```

To run the application:

```
make run
```

Best regards, Chris
<p>Contact: (310) 428-4312
