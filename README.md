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