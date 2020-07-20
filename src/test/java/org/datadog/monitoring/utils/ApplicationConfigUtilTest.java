package org.datadog.monitoring.utils;

import org.datadog.monitoring.ApplicationConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class ApplicationConfigUtilTest {

    @Test
    public void testValidUserProvidedArgs() {
        final String fileLocation = "/tmp/access.log";
        String []args = new String[] { "-r", "10", "-s", "5", "-a", "7", "-f", fileLocation };

        Optional<ApplicationConfig> configOptional = ApplicationConfigUtil.validateUserProvidedArgs(args);
        Assertions.assertTrue(configOptional.isPresent());

        ApplicationConfig applicationConfig = configOptional.get();
        Assertions.assertEquals(10, applicationConfig.getRequestsPerSecondThreshold());
        Assertions.assertEquals(5, applicationConfig.getStatsDisplayInterval());
        Assertions.assertEquals(7, applicationConfig.getAlertsMonitoringInterval());
        Assertions.assertEquals(fileLocation, applicationConfig.getLogFileLocation());
    }

    @Test
    public void testInvalidAlertsThresholdArg() {
        String []args = new String[] { "-s", "5", "-a", "4" };

        Assertions.assertThrows(IllegalArgumentException.class, () -> ApplicationConfigUtil.validateUserProvidedArgs(args));
    }

    @Test
    public void testEmptyArgs() {
        String []args = new String[] {};

        Optional<ApplicationConfig> configOptional = ApplicationConfigUtil.validateUserProvidedArgs(args);
        // if no args then we get default values
        Assertions.assertTrue(configOptional.isPresent());
    }
}
