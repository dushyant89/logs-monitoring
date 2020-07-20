package org.datadog.monitoring.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.datadog.monitoring.config.ApplicationConfig;
import org.datadog.monitoring.config.ApplicationConfigOptions;

import java.nio.file.Paths;
import java.util.Optional;

@UtilityClass
@Slf4j
public class ApplicationConfigUtil {
    private final Options options;
    private final CommandLineParser parser = new DefaultParser();
    private final String INVALID_ARGUMENT_VALUE = "Invalid %s argument value: %s";

    static {
        options = new Options()
                .addOption(ApplicationConfigOptions.FileLocation.getCliOption())
                .addOption(ApplicationConfigOptions.StatsInterval.getCliOption())
                .addOption(ApplicationConfigOptions.AlertsInterval.getCliOption())
                .addOption(ApplicationConfigOptions.RPSThreshold.getCliOption());
    }

    /**
     * Parses user provided args, validates them and builds the ApplicationConfig
     * @param args
     * @return On successful parsing and validation returns the ApplicationConfig
     */
    public Optional<ApplicationConfig> validateUserProvidedArgs(String []args) {
        Optional<CommandLine> commandLineOptional = parseUserArgs(args);

        if (commandLineOptional.isEmpty()) {
            new HelpFormatter().printHelp("monitor", options, true);

            return Optional.empty();
        }

        return Optional.of(getValidatedAppConfig(commandLineOptional.get()));
    }

    private Optional<CommandLine> parseUserArgs(String []args) {
        try {
            return Optional.of(parser.parse(options, args));
        } catch (ParseException e) {
            log.warn("Failed to parse command line arguments", e);
        }

        return Optional.empty();
    }

    private ApplicationConfig getValidatedAppConfig(CommandLine commandLine) {
        ApplicationConfig applicationConfig = new ApplicationConfig();

        String logFileLocationOption = commandLine.getOptionValue(ApplicationConfigOptions.FileLocation.getVerboseName());
        if (logFileLocationOption != null) {
            applicationConfig.setLogFileLocation(getValidatedLogFileLocation(logFileLocationOption));
        }

        String statsIntervalOption = commandLine.getOptionValue(ApplicationConfigOptions.StatsInterval.getVerboseName());
        if (statsIntervalOption != null) {
            applicationConfig.setStatsDisplayInterval(getValidatedStatsInterval(statsIntervalOption));
        }

        String alertsIntervalOption = commandLine.getOptionValue(ApplicationConfigOptions.AlertsInterval.getVerboseName());
        if (alertsIntervalOption != null) {
            applicationConfig.setAlertsMonitoringInterval(getValidatedAlertsInterval(alertsIntervalOption, applicationConfig.getStatsDisplayInterval()));
        }

        String RPSThresholdOption = commandLine.getOptionValue(ApplicationConfigOptions.RPSThreshold.getVerboseName());
        if (RPSThresholdOption != null) {
            applicationConfig.setRequestsPerSecondThreshold(getValidatedRPSThreshold(RPSThresholdOption));
        }

        return applicationConfig;
    }

    private String getValidatedLogFileLocation(String logFileLocationOption) {
        if (!Paths.get(logFileLocationOption).toFile().isFile()) {
            throw new IllegalArgumentException(
                String.format("Location `%s` should be a valid file location", logFileLocationOption)
            );
        }

        return logFileLocationOption;
    }

    private int getValidatedStatsInterval(String statsIntervalOption) {
        try {
            int statsDisplayInterval = Integer.parseInt(statsIntervalOption);
            if (statsDisplayInterval <= 0) { // @TODO: add minimum interval check
                throw new IllegalArgumentException(
                    String.format(INVALID_ARGUMENT_VALUE, ApplicationConfigOptions.StatsInterval.getVerboseName(), statsIntervalOption)
                );
            }

            return statsDisplayInterval;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                String.format(INVALID_ARGUMENT_VALUE, ApplicationConfigOptions.StatsInterval.getVerboseName(), statsIntervalOption)
            );
        }
    }

    private int getValidatedAlertsInterval(String alertsIntervalOption, int statsDisplayInterval) {
        try {
            int alertsMonitoringInterval = Integer.parseInt(alertsIntervalOption);
            if (alertsMonitoringInterval < statsDisplayInterval) {
                throw new IllegalArgumentException(
                    String.format(
                        "%s cannot be less than %s",
                        ApplicationConfigOptions.AlertsInterval.getVerboseName(),
                        ApplicationConfigOptions.StatsInterval.getVerboseName()
                    )
                );
            }

            return alertsMonitoringInterval;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                String.format(INVALID_ARGUMENT_VALUE, ApplicationConfigOptions.AlertsInterval.getVerboseName(), alertsIntervalOption)
            );
        }
    }

    private int getValidatedRPSThreshold(String RPSThresholdOption) {
        try {
            int RPSThreshold = Integer.parseInt(RPSThresholdOption);
            if (RPSThreshold <= 0) {
                throw new IllegalArgumentException(
                    String.format(INVALID_ARGUMENT_VALUE, ApplicationConfigOptions.RPSThreshold.getVerboseName(), RPSThresholdOption)
                );
            }

            return RPSThreshold;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                String.format(INVALID_ARGUMENT_VALUE, ApplicationConfigOptions.RPSThreshold.getVerboseName(), RPSThresholdOption)
            );
        }
    }
}
