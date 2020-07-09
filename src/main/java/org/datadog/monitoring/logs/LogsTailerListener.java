package org.datadog.monitoring.logs;

import org.apache.commons.io.input.TailerListenerAdapter;

public class LogsTailerListener extends TailerListenerAdapter {
    public void handle(String line) {
        System.out.println(line);
    }
}
