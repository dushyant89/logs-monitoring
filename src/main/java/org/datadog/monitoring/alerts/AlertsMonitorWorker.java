package org.datadog.monitoring.alerts;

import lombok.extern.slf4j.Slf4j;
import org.datadog.monitoring.ConsumerWorker;
import org.datadog.monitoring.traffic.TrafficSummary;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class AlertsMonitorWorker extends ConsumerWorker<TrafficSummary> {
    private final AlertsMonitor alertsMonitor;

    public AlertsMonitorWorker(BlockingQueue<TrafficSummary> inputQueue, BlockingQueue<String> messageQueue, AlertsMonitor alertsMonitor) {
        super(inputQueue, messageQueue);
        this.alertsMonitor = alertsMonitor;
    }

    public void run() {
        log.trace("AlertsMonitorWorker starting to run");

        while (true) {
            try {
                Optional<String> alertOutput = alertsMonitor.checkForAlert(inputQueue.take());
                alertOutput.ifPresent(this::handOutput);
            } catch (InterruptedException e) {
                log.warn("AlertsMonitorWorker got interrupted", e);
            }
        }
    }
}
