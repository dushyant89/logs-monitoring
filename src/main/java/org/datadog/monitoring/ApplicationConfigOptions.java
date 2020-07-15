package org.datadog.monitoring;

import lombok.Getter;
import org.apache.commons.cli.Option;

public enum ApplicationConfigOptions {
    FileLocation("f", "file-location", "location of the log file"),
    StatsInterval("s", "stats-interval", "time interval after which stats will be displayed"),
    AlertsInterval("a", "alerts-interval", "length of the time window for monitoring the alerts"),
    MaxRPS("r", "max-rps", "Max RPS after which alert will fire");

    @Getter
    private final String shortName;
    @Getter
    private final String verboseName;
    @Getter
    private final String description;

    ApplicationConfigOptions(String shortName, String verboseName, String description) {
        this.shortName = shortName;
        this.verboseName = verboseName;
        this.description = description;
    }

    public Option getCliOption() {
        return new Option(
                this.getShortName(),
                this.getVerboseName(),
                true,
                this.getDescription()
        );
    }
}
