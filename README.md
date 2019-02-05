# Coding challenge
**Car park Ubi**

## Domain vocabulary:
EV - electric vehicle.
CP - charging point, an element in an infrastructure that supplies electric energy for the recharging of electric vehicles

## Problem details:
The task is to implement a simple application to manage the charging points installed at Car park Ubi.
Car park Ubi has 10 charging points installed. When a car is connected it consumes either 20 Amperes(fast charging) or 10 Amperes (slow charging).
Car park Ubi installation has an overall current input of 100 Amperes so it can support fast charging for a maximum of 5 cars or slow charging for a maximum of 10 cars at one time.
A charge point sends notification to the application when car is plugged or unplugged. Also each charging point which has a car plugged to it is polling the
Application every 2 minutes to ask which current the charging  point is allowed to provide to a car - 10 or 20 Amperes.
The application must distribute the available current of 100 Amperes in between the charging points so that when possible all cars use fast charging and when the current is not sufficient some cars are switched to slow charging.
Cars which were connected earlier have lower priority than those which were connected later. The application must also provide a report with a current state of each charging point
returning a list of charging point, status (free or occupied) and - if occupied – the consumed current.

## Constraints:
The solution must be implemented as a Spring Boot application exposing a REST API.
We appreciate unit tests and an integration test for the happy path.

## Examples:

```
CP1 sends a notification that a car is plugged
Report:
CP1 OCCUPIED 20A
CP2 AVAILABLE
...
CP10 AVAILABLE
```

```
CP1, CP2, CP3, CP4, CP5 and CP6 send notification that a car is plugged
Report:
CP1 OCCUPIED 10A
CP2 OCCUPIED 10A
CP3 OCCUPIED 20A
CP4 OCCUPIED 20A
CP5 OCCUPIED 20A
CP6 OCCUPIED 20A
CP7 AVAILABLE
...
CP10 AVAILABLE
```

## Deliverables:
Link to the repository with the implementation and the documentation on how to call the API (Swagger/Postman collection/text description).
Please add any details about your ideas and considerations to this README and add it to the repository.

## Assumptions
- We trust the network for this application -> no encryption needed
- We trust the network is secured -> no authentication needed
- We trust the CPs not trying to cheat -> e.g. no verification needed
- Ubi is the only car park to manage 
- Amount of CPs, and the currents don't change often -> it's sufficient to change that in configuration file

## Considerations
- Added a persistence class, that persists the actual state of the CPs to a file, so that the applicatios can be restarted
- Charging points, currents, application port, path for persistence and logging can be configered via the application.yml

## Usage
To build the application run:

```
mvn clean package
```

To run it:
```
java -jar target/manager-0.0.1-SNAPSHOT.jar
```

### The application has four endpoints:
```
curl -X PUT http://localhost:8080/cp/plugin/<CP>
```
To tell the application a cur plugged in at <CP>, will return status code 200 on success, or 400 if e.g. <CP> is not configured.


```
curl -X PUT http://localhost:8080/cp/plugoff/<CP>
```
To tell the application a car was unplugged at <CP>, will return status code 200 on success, or 400 if e.g. <CP> is not configured.


```
curl http://localhost:8080/cp/current/<CP>
```
For the CPs to poll their actual current allowance, will return status code 200 and the current in Ampere in the body, or 400 if e.g. <CP> is not configured.


```
curl http://localhost:8080/park/report
```
Will return a plain text list of CPs with their name, current status and actual current if applicable.

see also swagger file