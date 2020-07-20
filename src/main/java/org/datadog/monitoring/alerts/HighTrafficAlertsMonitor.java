package org.datadog.monitoring.alerts;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.datadog.monitoring.traffic.TrafficSummary;

import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class HighTrafficAlertsMonitor extends AbstractAlertsMonitor {
    private final int averageRequestsThreshold;

    public HighTrafficAlertsMonitor(int alertsWindowSize, int averageRequestsThreshold) {
        super(alertsWindowSize, new Alert("HighTraffic"));
        this.averageRequestsThreshold = averageRequestsThreshold;
    }

    public Optional<String> checkForAlert(@NonNull TrafficSummary trafficSummary) {
        statsSummariesWindow.offer(trafficSummary);

        totalHitsPerMonitoringSession += trafficSummary.getTotalRequestCount();

        if (statsSummariesWindow.isAtFullCapacity()) {
            int averageHitsPerMonitoringSession = Math.round((float) totalHitsPerMonitoringSession / (float) statsSummariesWindow.maxSize());

            log.trace(String.format("averageHitsPerMonitoringSession: %d", averageHitsPerMonitoringSession));

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
