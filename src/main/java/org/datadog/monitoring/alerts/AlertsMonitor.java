package org.datadog.monitoring.alerts;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.datadog.monitoring.stats.StatsSummary;

public class AlertsMonitor {
    private final CircularFifoQueue<StatsSummary> statsSummariesWindow;
    private int totalHistPerMonitoringSession;
    private final int threshold;
    Alert alert;

    public AlertsMonitor(int alertsWindowSize, int threshold) {
        this.statsSummariesWindow = new CircularFifoQueue<>(alertsWindowSize);
        this.threshold = threshold;
        this.alert = new Alert(Alert.Type.HighTraffic);
    }

    public void processAlert(StatsSummary statsSummary) {
        statsSummariesWindow.offer(statsSummary);

        totalHistPerMonitoringSession += statsSummary.getTotalRequestCount();

        if (statsSummariesWindow.isAtFullCapacity()) {
            int averageHitsPerMonitoringSession = Math.round((float) totalHistPerMonitoringSession / statsSummariesWindow.maxSize());

            totalHistPerMonitoringSession -= statsSummariesWindow.remove().getTotalRequestCount();

            if (averageHitsPerMonitoringSession > threshold) {
                if (!alert.getAlertSate().equals(Alert.State.Active)) {
                    alert.setAlertSate(Alert.State.Active);
                    alert.trigger();
                }

                return;
            }

            if (averageHitsPerMonitoringSession < threshold) {
                if (alert.getAlertSate().equals(Alert.State.Active)) {
                    alert.setAlertSate(Alert.State.Recovered);
                    alert.trigger();

                    return;
                }

                if (alert.getAlertSate().equals(Alert.State.Recovered)) {
                    alert.setAlertSate(Alert.State.Inactive);
                }
            }
        }
    }
}
