package org.datadog.monitoring.alerts;

import org.datadog.monitoring.SimpleConsumer;
import org.datadog.monitoring.stats.StatsSummary;

import java.util.concurrent.BlockingQueue;

public class AlertsConsumer extends SimpleConsumer<StatsSummary> {
    private final AlertsMonitor alertsMonitor;

    public AlertsConsumer(BlockingQueue<StatsSummary> inputQueue, int alertsWindowSize, int threshold) {
        super(inputQueue);
        this.alertsMonitor = new AlertsMonitor(alertsWindowSize, threshold);
    }

    public void run() {
        while (true) {
            try {
                alertsMonitor.processAlert(inputQueue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
