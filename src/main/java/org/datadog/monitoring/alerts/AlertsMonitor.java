package org.datadog.monitoring.alerts;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.datadog.monitoring.stats.StatsSummary;

public class AlertsMonitor {
    private final CircularFifoQueue<StatsSummary> statsSummariesWindow;
    private int totalHistPerMonitoringSession;
    private final int threshold;

    public AlertsMonitor(int alertsWindowSize, int threshold) {
        this.statsSummariesWindow = new CircularFifoQueue<>(alertsWindowSize);
        this.threshold = threshold;
    }

    public void processAlert(StatsSummary statsSummary) {
        if (statsSummariesWindow.isFull()) {
            totalHistPerMonitoringSession -= statsSummariesWindow.remove().getTotalRequestCount();
        }

        statsSummariesWindow.offer(statsSummary);

        totalHistPerMonitoringSession += statsSummary.getTotalRequestCount();

        int averageHitsPerMonitoringSession = Math.round((float) totalHistPerMonitoringSession / statsSummariesWindow.maxSize());

        if (averageHitsPerMonitoringSession > threshold) {
            System.out.println("High traffic alert generated!!!!!");
        }
    }
}
