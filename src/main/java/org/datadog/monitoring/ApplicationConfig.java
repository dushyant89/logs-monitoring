package org.datadog.monitoring;

import lombok.Data;

@Data
public class ApplicationConfig {
    // location from which the logs will be tailed
    private String logFileLocation = "/tmp/access.log";

    // In seconds
    private int statsDisplayInterval = 10;

    // In seconds
    private int alertsMonitoringInterval = 120;

    // Max RPS after which we will fire alert
    private int requestsPerSecondThreshold = 10;
}
