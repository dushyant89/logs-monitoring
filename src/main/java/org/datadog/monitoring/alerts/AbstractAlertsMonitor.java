package org.datadog.monitoring.alerts;

import lombok.Data;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.datadog.monitoring.traffic.TrafficSummary;

/**
 * Class which specifies the basic layout for any sort of alert
 * which we would like to monitor.
 */
@Data
public abstract class AbstractAlertsMonitor implements AlertsMonitor {
    // Circular queue which stores the stats summaries
    protected final CircularFifoQueue<TrafficSummary> statsSummariesWindow;
    // total no. of hits which we have monitored since app start.
    protected int totalHitsPerMonitoringSession;
    // The alert which we will monitor
    protected Alert alert;

    public AbstractAlertsMonitor() {
        statsSummariesWindow = new CircularFifoQueue<>();
    }

    public AbstractAlertsMonitor(int alertsWindowSize, Alert alert) {
        this.statsSummariesWindow = new CircularFifoQueue<>(alertsWindowSize);
        this.alert = alert;
    }
}
