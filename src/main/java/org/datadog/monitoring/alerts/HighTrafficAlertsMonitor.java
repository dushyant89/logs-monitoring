package org.datadog.monitoring.alerts;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.datadog.monitoring.stats.StatsSummary;

import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Data
public class HighTrafficAlertsMonitor extends AbstractAlertsMonitor {
    private final int averageRequestsThreshold;

    public HighTrafficAlertsMonitor(int alertsWindowSize, int averageRequestsThreshold) {
        super(alertsWindowSize, new Alert("HighTraffic"));
        this.averageRequestsThreshold = averageRequestsThreshold;
    }

    public Optional<String> checkForAlert(@NonNull StatsSummary statsSummary) {
        statsSummariesWindow.offer(statsSummary);

        totalHitsPerMonitoringSession += statsSummary.getTotalRequestCount();

        if (statsSummariesWindow.isAtFullCapacity()) {
            int averageHitsPerMonitoringSession = Math.round((float) totalHitsPerMonitoringSession / (float) statsSummariesWindow.maxSize());

            totalHitsPerMonitoringSession -= statsSummariesWindow.remove().getTotalRequestCount();

            if (averageHitsPerMonitoringSession > averageRequestsThreshold) {
                if (!alert.getAlertSate().equals(Alert.State.Active)) {
                    alert.setAlertSate(Alert.State.Active);

                    return Optional.of(String.format(ALERT_MESSAGE, alert.getName(), alert.getAlertSate().name()));
                }
            }

            if (averageHitsPerMonitoringSession <= averageRequestsThreshold) {
                if (alert.getAlertSate().equals(Alert.State.Active)) {
                    alert.setAlertSate(Alert.State.Recovered);

                    return Optional.of(String.format(ALERT_MESSAGE, alert.getName(), alert.getAlertSate().name()));
                }

                if (alert.getAlertSate().equals(Alert.State.Recovered)) {
                    alert.setAlertSate(Alert.State.Inactive);
                }
            }
        }

        return Optional.empty();
    }
}
