package org.datadog.monitoring.stats;

import org.datadog.monitoring.SimpleWorker;
import org.datadog.monitoring.alerts.AlertsMonitor;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;

public class StatsSummaryWorker extends SimpleWorker<StatsSummary> {
    private final AlertsMonitor alertsMonitor;

    public StatsSummaryWorker(BlockingQueue<StatsSummary> inputQueue, BlockingQueue<String> messageQueue, int alertsWindowSize, int threshold) {
        super(inputQueue, messageQueue);
        this.alertsMonitor = new AlertsMonitor(alertsWindowSize, threshold);
    }

    public void run() {
        while (true) {
            try {
                Optional<String> alertOutput = alertsMonitor.processAlert(inputQueue.take());
                alertOutput.ifPresent(this::handOutput);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
