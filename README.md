# Datadog Logs Monitoring App

This app listens/tails the access logs from a location like `/tmp/access.log` and generates statistics and alerts for the traffic.

## Requirements
* maven
* java runtime environment 11

## Installation
The below command will install all the necessary libraries required for the app to run.

`mvn install`

## Test

`mvn test`

## Execution

Runs the app with default configuration:

`./run.sh`

For e.g. to run with custom log location:

`./run.sh -f /var/access.log`

To see all configuration options:

`./run.sh --help`

## Logging

The application during its execution logs information from logging levels ranging from `TRACE` to `ERROR` in `application.log` file in the project root

## Application Design

![App Architecture](app-architecture.png)

* The application is composed of several workers which are started by the main class i.e. `MonitoringApplication`. 
* Each worker is based on `single responsibility` principle.
* The workers are connected by unbounded FIFO queues (represented by thick arrow). The work output of a worker can handed to the next worker in the sequence.
* If a worker's output need to be displayed to the user they use the `PrintMessageWorker` which currently simple logs to the console.