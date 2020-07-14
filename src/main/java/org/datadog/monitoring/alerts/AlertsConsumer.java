package org.datadog.monitoring.alerts;

import org.datadog.monitoring.stats.StatsSummary;

import java.util.concurrent.BlockingQueue;

public class AlertsConsumer implements Runnable {
    private final BlockingQueue<StatsSummary> statsSummariesQueue;
    private final AlertsMonitor alertsMonitor;

    public AlertsConsumer(BlockingQueue<StatsSummary> statsSummariesQueue, int alertsWindowSize, int threshold) {
        this.statsSummariesQueue = statsSummariesQueue;
        this.alertsMonitor = new AlertsMonitor(alertsWindowSize, threshold);
    }

    public void run() {
        while (true) {
            try {
                alertsMonitor.processAlert(statsSummariesQueue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
