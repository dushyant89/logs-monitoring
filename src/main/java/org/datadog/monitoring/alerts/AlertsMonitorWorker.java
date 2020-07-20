package org.datadog.monitoring.alerts;

import lombok.extern.slf4j.Slf4j;
import org.datadog.monitoring.ConsumerWorker;
import org.datadog.monitoring.stats.StatsSummary;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class AlertsMonitorWorker extends ConsumerWorker<StatsSummary> {
    private final AlertsMonitor alertsMonitor;

    public AlertsMonitorWorker(BlockingQueue<StatsSummary> inputQueue, BlockingQueue<String> messageQueue, AlertsMonitor alertsMonitor) {
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
