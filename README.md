# Datadog Logs Monitoring App

This app listens/tails the access logs from a location like `/tmp/access.log` and generates simple statistics and alerts for the traffic.

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

```
usage: monitor [-a <arg>] [-f <arg>] [-r <arg>] [-s <arg>]
 -a,--alerts-interval <arg>   length of the time window for monitoring the
                              alerts
 -f,--file-location <arg>     location of the log file
 -r,--max-rps <arg>           maximum requests per second threshold, after
                              which alert will fire
 -s,--stats-interval <arg>    time interval after which stats will be
                              displayed
```

To see alerts being active and recovered in quick succession, try:

`./run.sh -s 1 -a 1 -r 5`

Assuming 5 requests per second is the threshold which is enough.

### Sample output

```
****** Traffic stats  ******
Total requests served: 6
Total content size: 29 KB
Top 5 sections by hits:
	posts -> 2
	app -> 1
	explore -> 1
	wp-content -> 1
	apps -> 1
HTTP Methods by hits:
	GET -> 3
	PUT -> 1
	POST -> 2
****** End of traffic stats ******

	!!! A HighTraffic is now Active !!!

****** Traffic stats  ******
Total requests served: 5
Total content size: 24 KB
Top 5 sections by hits:
	explore -> 2
	app -> 1
	posts -> 1
	wp-admin -> 1
HTTP Methods by hits:
	GET -> 2
	DELETE -> 1
	POST -> 1
	PUT -> 1
****** End of traffic stats ******

	!!! A HighTraffic is now Recovered !!!
```

## Logging

The application during its execution logs information from logging levels ranging from `TRACE` to `ERROR` in `application.log` file in the project root.

## Application Design

![App Architecture](app-architecture.png)

* The application is composed of several workers which are started by the main class i.e. `MonitoringApplication`. 
* Each worker is based on the `single responsibility` principle.
* The workers are connected by unbounded FIFO queues (represented by thick arrow). The work output of a worker can be handed to the next worker in the sequence.
* Workers which operate in a sequence act as `producer` & `consumer` respectively for e.g. `LogsParserWorker` is a producer which gives parsed logs to the `TrafficSummaryGeneratorWorker`.
* If a worker's output need to be displayed to the user they use the `PrintMessageWorker` which currently simply logs to the console.

## Application Flow

### LogsListener

* Tails the log file for new logs which are appended to the end of the file.
* New log lines are added to an unbounded queue on which `LogsParserWorker` is blocked.

### LogsParserWorker

* A scheduled worker which runs every `stats-interval` seconds.
* So, if the time window of displaying stats is 10 seconds, this worker will *once every 10 seconds*.
* It empties the queue completely and parses the log lines and hands over to the `TrafficSummaryGeneratorWorker`.

### TrafficSummaryGeneratorWorker

* It waits on the parsed log lines and generates the traffic summary for the log lines it gets.
* The generated traffic summary is printed and also sent to the next worker i.e `AlertsMonitorWorker`

### AlertsMonitorWorker

* Receives the traffic summary and stores in a circular FIFO queue.
* Once the queue is full, the average requests per seconds are calculated and compared agains the threshold.
* If the threshold is crossed, we raise an alert.

## Fault tolerance

* Any sort of exceptions which can occur are caught and logged.
* Each worker running is as a separate thread and if any thread terminates due to a failure during execution prior to shutdown, a new one will take its place if needed to execute subsequent tasks.

## Availability

The application will keep running unless the user quits the application.

## Improvements

* Building a nice UI to show the traffic summary and alerts, currently everything goes to the console and you need to scroll to see any previous alerts which were active or recovered.
* Implementing more log formats like `Combined Log Format`, `Extended Log File Format` etc. but any new format can be added easily by implementing the  `LogsParser` interface.
* Currently, the pattern matching for `Common Log Format` is very rudimentary and it doesn't specifically match for ipaddress or hostnames e.g.
* Adding more alert monitors for e.g. `LowTrafficAlertsMonitor` when the traffic drops below a specific moving average.
* Adding more elements to the traffic summary for e.g. some stats about the response codes.
* There is no persistence for the traffic summary and alerts that we are generate. Connecting to a high throughput datastore like `Cassandra` will be good.
* Improving the test coverage, couldn't add more tests due to time constraints.