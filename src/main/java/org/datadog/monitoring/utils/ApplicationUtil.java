package org.datadog.monitoring.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.datadog.monitoring.ApplicationConfig;
import org.datadog.monitoring.ApplicationConfigOptions;

import java.nio.file.Paths;
import java.util.Optional;

@UtilityClass
@Slf4j
public class ApplicationUtil {
    private final Options options;
    private final CommandLineParser parser = new DefaultParser();
    private final String INVALID_ARGUMENT_VALUE = "Invalid %s argument value: %s";

    static {
        options = new Options()
                .addOption(ApplicationConfigOptions.FileLocation.getCliOption())
                .addOption(ApplicationConfigOptions.StatsInterval.getCliOption())
                .addOption(ApplicationConfigOptions.AlertsInterval.getCliOption())
                .addOption(ApplicationConfigOptions.MaxRPS.getCliOption());
    }

    public Optional<ApplicationConfig> validateUserProvidedArgs(String []args) {
        Optional<CommandLine> commandLineOptional = parseUserArgs(args);

        if (commandLineOptional.isEmpty()) {
            new HelpFormatter().printHelp("monitor", options, true);

            return Optional.empty();
        }

        return Optional.of(getValidatedAppConfig(commandLineOptional.get()));
    }

    private ApplicationConfig getValidatedAppConfig(CommandLine commandLine) {
        ApplicationConfig applicationConfig = new ApplicationConfig();

        String logFileLocationOption = commandLine.getOptionValue(ApplicationConfigOptions.FileLocation.getVerboseName());
        if (logFileLocationOption != null) {
            applicationConfig.setLogFileLocation(logFileLocationOption);

            if (!Paths.get(logFileLocationOption).toFile().isFile()) {
                throw new IllegalArgumentException(String.format("Location `%s` should be a valid file location", logFileLocationOption));
            }
        }

        String statsIntervalOption = commandLine.getOptionValue(ApplicationConfigOptions.StatsInterval.getVerboseName());
        if (statsIntervalOption != null) {
            try {
                int statsDisplayInterval = Integer.parseInt(statsIntervalOption);
                if (statsDisplayInterval < 1) {
                    throw new IllegalArgumentException(String.format(INVALID_ARGUMENT_VALUE, ApplicationConfigOptions.StatsInterval.getVerboseName(), statsIntervalOption));
                }
                applicationConfig.setStatsDisplayInterval(statsDisplayInterval);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(String.format(INVALID_ARGUMENT_VALUE, ApplicationConfigOptions.StatsInterval.getVerboseName(), statsIntervalOption));
            }
        }

        String alertsIntervalOption = commandLine.getOptionValue(ApplicationConfigOptions.AlertsInterval.getVerboseName());
        if (alertsIntervalOption != null) {
            try {
                int alertsMonitoringInterval = Integer.parseInt(alertsIntervalOption);
                if (alertsMonitoringInterval < applicationConfig.getStatsDisplayInterval()) {
                    throw new IllegalArgumentException(
                            String.format(
                                    "%s should be bigger than %s! Got %s and %d.",
                                    ApplicationConfigOptions.AlertsInterval.getVerboseName(),
                                    ApplicationConfigOptions.StatsInterval.getVerboseName(),
                                    alertsMonitoringInterval, applicationConfig.getStatsDisplayInterval()));
                }
                applicationConfig.setAlertsMonitoringInterval(alertsMonitoringInterval);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(String.format(INVALID_ARGUMENT_VALUE, ApplicationConfigOptions.AlertsInterval.getVerboseName(), alertsIntervalOption));
            }
        }

        String RPSThresholdOption = commandLine.getOptionValue(ApplicationConfigOptions.MaxRPS.getVerboseName());
        if (RPSThresholdOption != null) {
            try {
                int RPSThreshold = Integer.parseInt(RPSThresholdOption);
                if (RPSThreshold < 1) {
                    throw new IllegalArgumentException(String.format(INVALID_ARGUMENT_VALUE, ApplicationConfigOptions.MaxRPS.getVerboseName(), RPSThresholdOption));
                }
                applicationConfig.setRequestsPerSecondThreshold(RPSThreshold);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(String.format(INVALID_ARGUMENT_VALUE, ApplicationConfigOptions.MaxRPS.getVerboseName(), RPSThresholdOption));
            }
        }

        return applicationConfig;
    }

    private Optional<CommandLine> parseUserArgs(String []args) {
        try {
            return Optional.of(parser.parse(options, args));
        } catch (ParseException e) {
            log.warn("Failed to parse command line arguments", e);
        }

        return Optional.empty();
    }
}
