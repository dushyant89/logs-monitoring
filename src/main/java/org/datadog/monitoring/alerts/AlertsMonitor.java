package org.datadog.monitoring.alerts;

import lombok.Data;
import lombok.NonNull;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.datadog.monitoring.stats.StatsSummary;

import java.util.Optional;

@Data
public class AlertsMonitor {
    private final CircularFifoQueue<StatsSummary> statsSummariesWindow;
    private int totalHistPerMonitoringSession;
    private final int threshold;
    private Alert alert;

    public AlertsMonitor(int alertsWindowSize, int threshold) {
        this.statsSummariesWindow = new CircularFifoQueue<>(alertsWindowSize);
        this.threshold = threshold;
        this.alert = new Alert(Alert.Type.HighTraffic);
    }

    public Optional<String> processAlert(@NonNull StatsSummary statsSummary) {
        statsSummariesWindow.offer(statsSummary);

        totalHistPerMonitoringSession += statsSummary.getTotalRequestCount();

        if (statsSummariesWindow.isAtFullCapacity()) {
            int averageHitsPerMonitoringSession = Math.round((float) totalHistPerMonitoringSession / statsSummariesWindow.maxSize());

            totalHistPerMonitoringSession -= statsSummariesWindow.remove().getTotalRequestCount();

            if (averageHitsPerMonitoringSession > threshold) {
                if (!alert.getAlertSate().equals(Alert.State.Active)) {
                    alert.setAlertSate(Alert.State.Active);

                    return Optional.of(alert.toString());
                }
            }

            if (averageHitsPerMonitoringSession < threshold) {
                if (alert.getAlertSate().equals(Alert.State.Active)) {
                    alert.setAlertSate(Alert.State.Recovered);

                    return Optional.of(alert.toString());
                }

                if (alert.getAlertSate().equals(Alert.State.Recovered)) {
                    alert.setAlertSate(Alert.State.Inactive);
                }
            }
        }

        return Optional.empty();
    }
}
