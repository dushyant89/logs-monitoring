package org.datadog.monitoring.logs;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.TailerListenerAdapter;

import java.util.concurrent.BlockingQueue;

@Slf4j
public class LogsListener extends TailerListenerAdapter {
    BlockingQueue<String> logsProducer;

    public LogsListener(BlockingQueue<String> logsProducer) {
        this.logsProducer = logsProducer;
    }

    public void handle(String line) {
        logsProducer.offer(line);
    }

    public void fileNotFound() {
        log.error("Log file not found");
    }

    public void handle(Exception ex) {
        log.warn(ex.getMessage());
    }
}
