# Bird Engineering: Coding Exercise

Thank you for your continued interest in Bird!

This at-home coding challenge is an opportunity for you to write some clean code that shows us how you use data structures to solve algorithmic problems.

  * Write code as if you were shipping it: Assume you are going to be code reviewed, articulate a test plan, etc.
  * You may use any programming language of your choice.
  * Use your preferred IDE or editor and whatever tooling you're comfortable with (including Xcode for iOS applicants).
  * Feel free to use whatever references you'd like, including Google.
  * Your solution does not have to persist data between runs.
  * When you’re finished, make sure your code is committed to the repo with instructions on how to run it.
  * Please press the submit button in the top-right corner in Py to let us know you're done.
  * Your solution should be self-contained and not require additional software to run it.
  * Got an idea for a cool feature to add? Do it! We love seeing your creative side

Please complete an implementation to support the basic commands as these are required, but if you’re up for a challenge, we are also offering up a few additional commands and constraints to consider as a bonus.

## Problem Statement

At Bird we're expanding into so many cities that we need to write some tools to help us make sure birds are where they need to be, when they need to be there.
For this exercise, we'll be writing a command-based program to schedule bird batches. A bird batch is a group of birds that are transported and deployed together, so a bird batch consists of a batch ID and a count of birds in the batch. Bird batches are moved between cities as a unit. A city is modeled as a name with a latitude/longitude pair. Some cities also have an additional value, an integer cap on the number of birds allowed by government officials. Batches of birds are scheduled to provide service in a city at a particular start date, and remain in the city until a given end date.

Your program should accept this information on standard in by parsing the following commands and return its answers on standard out.

## Commands

In the following sections, the commands are case insensitive. Things inside angle brackets are meant to indicate user-supplied input, and a question mark indicates an optional item, but everything else should be literal syntax.

```
CITY "<NAME>" <LATITUDE> <LONGITUDE> <CAP>?
- NAME : String
- LATITUDE: Float
- LONGITUDE: Float
- CAP: Int
```

Enters the city information of the city with the given name, latitude, longitude, and optional cap (The cap can be left off if there is no cap in this city).

```
BATCH <ID> <COUNT>
- ID : Int
- COUNT: Int
```

Creates a batch with the given ID and number of birds.

```
SCHEDULE <BATCHID> "<CITY>" <STARTDATE> <ENDDATE>
- BATCHID: Int
- CITY: String
- STARTDATE: String with ISO8601-formatted content
- ENDDATE: String with ISO8601-formatted content
```

Schedules a batch to be deployed in the named already-known city for the period of time between the start and end dates. The start and end dates should be given by ISO8601 datetime strings.

If a schedule request cannot be accommodated, the program should print an error and refuse to schedule the batch deployment. Scheduling is subject to the natural constraints provided by the batch's availability and any city caps that may apply.

```
CANCEL <BATCHID> "<CITY>" <DATE>
- BATCHID: Int
- CITY: String
- DATE: String with ISO8601-formatted content
```

Deschedule a batch with the given batch ID from the named city at the given date. The given date can be any ISO8601 datetime between the existing scheduled deployment's start and end dates.

### SHOW CITIES

Prints all of the deployments scheduled, grouped by city.

### SHOW BATCHES

Prints all of the deployments scheduled, group by batch.

### Example Session

```
CITY "Los Angeles" 34.048925 -118.428663 CITY "Austin" 30.305804 -97.728682 500
BATCH 1 250
BATCH 2 500
BATCH 3 200
SCHEDULE 1 "Austin" 2018-08-31T00:44:40+00:00 2018-09-24T00:44:40+00:00
SCHEDULE 2 "Austin" 2018-08-31T00:44:40+00:00 2018-09-24T00:44:40+00:00
← ERROR
SCHEDULE 3 "Austin" 2018-08-31T00:44:40+00:00 2018-09-24T00:44:40+00:00
SHOW CITIES
← Austin
BATCH(1, 250) -> 2018-08-31T00:44:40+00:00 2018-09-24T00:44:40+00:00
BATCH(3, 200) -> 2018-08-31T00:44:40+00:00 2018-09-24T00:44:40+00:00
← Los Angeles
SCHEDULE 2 "Austin" 2018-09-25T00:00:00+00:00 2018-10-18T12:00:00+00:00
SHOW CITIES
← Austin
BATCH(1, 250) -> 2018-08-31T00:44:40+00:00 2018-09-24T00:44:40+00:00
BATCH(2, 500) -> 2018-09-25T00:00:00+00:00 2018-10-18T12:00:00+00:00
BATCH(3, 200) -> 2018-08-31T00:44:40+00:00 2018-09-24T00:44:40+00:00
← Los Angeles
CANCEL 3 "Austin" 2018-08-31T00:44:41+00:00
SHOW BATCHES
← BATCH 1
← Austin 2018-08-31T00:44:40+00:00 - 2018-09-24T00:44:40+00:00
← BATCH 2
← Austin 2018-09-25T00:00:00+00:00 2018-10-18T12:00:00+00:00
← BATCH 3
 SCHEDULE 2 "Los Angeles" 2018-09-26T00:00:00+00:00 2018-09-30T12:00:00+00:00
← ERROR
SCHEDULE 2 "Los Angeles" 2018-10-20T12:00:00+00:00 2018-12-17T12:00:00+00:00
SHOW CITIES
← Austin
BATCH(1, 250) -> 2018-08-31T00:44:40+00:00 2018-09-24T00:44:40+00:00
BATCH(2, 500) -> 2018-09-25T00:00:00+00:00 2018-10-18T12:00:00+00:00
← Los Angeles
BATCH(2, 500) -> 2018-10-20T12:00:00+00:00 2018-12-17T12:00:00+00:00
```

## Bonus

### Account for Transit Time

Batches can't actually move between cities instantaneously. We should actually be taking into account the time it takes to move a batch from place to place. Assume batches can be moved at a constant rate of 50kph. Make the program additionally check that a given bird deployment is compatible with the travel time between cities.

Time-specific Queries
Update the SHOW command to take additional arguments, as follows.

```
SHOW CITY "<CITY>"
- CITY: String
```

Prints a list of the batches scheduled to be deployed at the given city.

```
SHOW BATCH <BATCHID>
- BATCHID: Int
```

Prints a list of the deployments scheduled for the given batch ID.

## Notes
For distance calculations, assume the earth is flat and assume there are 111km per degree of latitude or longitude in this system. Assume that there is no friction in transporting, deploying, and undeploying birds; that is, the birds will leave cities at the exact moment they're scheduled to.
