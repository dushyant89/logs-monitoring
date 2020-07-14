package org.datadog.monitoring.alerts;

import org.datadog.monitoring.SimpleConsumer;
import org.datadog.monitoring.stats.StatsSummary;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;

public class AlertsConsumer extends SimpleConsumer<StatsSummary> {
    private final AlertsMonitor alertsMonitor;
    private final BlockingQueue<String> outputQueue;

    public AlertsConsumer(BlockingQueue<StatsSummary> inputQueue, BlockingQueue<String> outputQueue, int alertsWindowSize, int threshold) {
        super(inputQueue);
        this.outputQueue = outputQueue;
        this.alertsMonitor = new AlertsMonitor(alertsWindowSize, threshold);
    }

    public void run() {
        while (true) {
            try {
                Optional<String> alertOutput = alertsMonitor.processAlert(inputQueue.take());
                alertOutput.ifPresent(outputQueue::offer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
