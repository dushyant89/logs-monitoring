package org.datadog.monitoring.stats;

import lombok.extern.slf4j.Slf4j;
import org.datadog.monitoring.SimpleWorker;
import org.datadog.monitoring.alerts.AlertsMonitor;
import org.datadog.monitoring.alerts.HighTrafficAlertsMonitor;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class StatsSummaryWorker extends SimpleWorker<StatsSummary> {
    private final AlertsMonitor highTrafficAlertsMonitor;

    public StatsSummaryWorker(BlockingQueue<StatsSummary> inputQueue, BlockingQueue<String> messageQueue, int alertsWindowSize, int threshold) {
        super(inputQueue, messageQueue);
        highTrafficAlertsMonitor = new HighTrafficAlertsMonitor(alertsWindowSize, threshold);
    }

    public void run() {
        log.trace("StatsSummaryWorker starting to run");

        while (true) {
            try {
                Optional<String> alertOutput = highTrafficAlertsMonitor.checkForAlert(inputQueue.take());
                alertOutput.ifPresent(this::handOutput);
            } catch (InterruptedException e) {
                log.warn("StatsSummaryWorker got interrupted", e);
            }
        }
    }
}
