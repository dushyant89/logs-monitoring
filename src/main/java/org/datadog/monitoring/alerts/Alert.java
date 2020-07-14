package org.datadog.monitoring.alerts;

import lombok.Data;

@Data
public class Alert {
    private State alertSate;
    private Type alertType;

    public Alert(Type type) {
        alertSate = State.Inactive;
        alertType = type;
    }

    public enum State {
        Active,
        Inactive,
        Recovered,
    }

    public enum Type {
        HighTraffic("High traffic alert"),
        LowTraffic("Low traffic alert"),
        ;

        private final String description;

        Type(String description) {
            this.description = description;
        }
    }

    public void trigger() {
        if (alertSate != State.Inactive) {
            System.out.println(String.format("\t!!! A %s is now %s !!!", alertType.description, alertSate.name()));
        }
    }
}
