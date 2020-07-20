package org.datadog.monitoring.alerts;


import org.datadog.monitoring.traffic.TrafficSummary;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;


public class HighTrafficAlertsMonitorTest {
    private HighTrafficAlertsMonitor highTrafficAlertsMonitor;

    private static final int ALERTS_WINDOW_SIZE = 5;
    private static final int ALERTS_THRESHOLD = 10;

    @BeforeEach
    public void setupAlertsMonitor() {
        // Setup alerts monitor with window size of 5 stats and threshold of 10 requests per second
        highTrafficAlertsMonitor = new HighTrafficAlertsMonitor(ALERTS_WINDOW_SIZE, ALERTS_THRESHOLD);
    }

    @Test
    public void testAlertResultWhenWindowIsNotFull() {
        Optional<String> processAlertResult;
        for (int i=0; i < ALERTS_WINDOW_SIZE - 1; i++) {
            processAlertResult = highTrafficAlertsMonitor.checkForAlert(new TrafficSummary(ALERTS_THRESHOLD));
            // No alert should be there since the window is not at full capacity
            Assertions.assertTrue(processAlertResult.isEmpty());
        }
    }

    @Test
    public void testAlertResultWhenWindowIsFull() {
        Optional<String> processAlertResult;

        addStatsSummaries(ALERTS_WINDOW_SIZE - 1, ALERTS_THRESHOLD);

        // The average no. of requests will now be over the threshold
        processAlertResult = highTrafficAlertsMonitor.checkForAlert(new TrafficSummary(ALERTS_THRESHOLD + ALERTS_WINDOW_SIZE));
        Assertions.assertTrue(processAlertResult.isPresent());
    }

    @Test
    public void testAlertIsRecovered() {
        Optional<String> processAlertResult;
        addStatsSummaries(ALERTS_WINDOW_SIZE, ALERTS_THRESHOLD + 1);

        Assertions.assertEquals(highTrafficAlertsMonitor.getAlert().getAlertSate(), Alert.State.Active);
        // bring the moving average below the threshold
        processAlertResult = highTrafficAlertsMonitor.checkForAlert(new TrafficSummary(ALERTS_THRESHOLD - ALERTS_WINDOW_SIZE));
        Assertions.assertTrue(processAlertResult.isPresent());
        Assertions.assertEquals(highTrafficAlertsMonitor.getAlert().getAlertSate(), Alert.State.Recovered);
    }

    @Test
    // Tests the below state transition
    // Active -> Recovered -> Inactive
    public void testAlertIsInActiveFromRecovered() {
        Optional<String> processAlertResult;
        addStatsSummaries(ALERTS_WINDOW_SIZE, ALERTS_THRESHOLD + 1);

        Assertions.assertEquals(highTrafficAlertsMonitor.getAlert().getAlertSate(), Alert.State.Active);
        // bring the moving average below the threshold
        processAlertResult = highTrafficAlertsMonitor.checkForAlert(new TrafficSummary(ALERTS_THRESHOLD - ALERTS_WINDOW_SIZE));
        Assertions.assertTrue(processAlertResult.isPresent());
        Assertions.assertEquals(highTrafficAlertsMonitor.getAlert().getAlertSate(), Alert.State.Recovered);

        processAlertResult = highTrafficAlertsMonitor.checkForAlert(new TrafficSummary(ALERTS_THRESHOLD));
        Assertions.assertTrue(processAlertResult.isEmpty());
        Assertions.assertEquals(highTrafficAlertsMonitor.getAlert().getAlertSate(), Alert.State.Inactive);
    }

    private void addStatsSummaries(int count, int requestCount) {
        for (int i=0; i < count; i++) {
            highTrafficAlertsMonitor.checkForAlert(new TrafficSummary(requestCount));
        }
    }
}
