package org.datadog.monitoring.alerts;

import lombok.Data;

@Data
public class Alert {
    // the current state in which alert is
    private State alertSate;

    private String name;

    public Alert(String name) {
        alertSate = State.Inactive;
        this.name = name;
    }

    public enum State {
        Active,
        Inactive,
        Recovered,
    }
}
