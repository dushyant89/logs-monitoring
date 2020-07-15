package org.datadog.monitoring.ui;

import lombok.extern.slf4j.Slf4j;
import org.datadog.monitoring.SimpleWorker;

import java.util.concurrent.BlockingQueue;

@Slf4j
public class PrintMessageWorker extends SimpleWorker<String> {
    public PrintMessageWorker(BlockingQueue<String> inputQueue) {
        super(inputQueue);
    }

    public void run() {
        while (true) {
            try {
                System.out.println(inputQueue.take());
            } catch (InterruptedException e) {
                log.warn("PrintMessageWorker is interrupted", e);
            }
        }
    }
}
