package org.datadog.monitoring.ui;

import org.datadog.monitoring.SimpleWorker;

import java.util.concurrent.BlockingQueue;

public class OutputMessageWorker extends SimpleWorker<String> {
    public OutputMessageWorker(BlockingQueue<String> inputQueue) {
        super(inputQueue);
    }

    public void run() {
        while (true) {
            try {
                System.out.println(inputQueue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
