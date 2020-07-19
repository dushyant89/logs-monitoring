package org.datadog.monitoring.alerts;

import org.datadog.monitoring.stats.StatsSummary;

import java.util.Optional;

public interface AlertsMonitor {
    // Message which can be used when a alert is processed.
    String ALERT_MESSAGE = "\t!!! A %s is now %s !!!\n";

    /**
     * Based on the information we get from the stats summary we check if
     * there is a criteria satisfied for an alert or not.
     *
     * @param statsSummary Summary of the traffic stats in some time window.
     * @return The result of the checks performed on the summary for an alert.
     */
    Optional<String> checkForAlert(StatsSummary statsSummary);
}
