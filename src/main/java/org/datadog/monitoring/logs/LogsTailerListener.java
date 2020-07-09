package org.datadog.monitoring.logs;

import org.apache.commons.io.input.TailerListenerAdapter;

import java.util.concurrent.BlockingQueue;

public class LogsTailerListener extends TailerListenerAdapter {
    BlockingQueue<String> logsProducer;

    public LogsTailerListener(BlockingQueue<String> logsProducer) {
        this.logsProducer = logsProducer;
    }

    public void handle(String line) {
        logsProducer.offer(line);
    }

    public void fileNotFound() {
        System.out.println("File not found");
    }

    public void handle(Exception ex) {
        System.out.println(ex.getMessage());
    }
}
