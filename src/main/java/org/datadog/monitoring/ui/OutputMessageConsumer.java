package org.datadog.monitoring.ui;

import org.datadog.monitoring.SimpleConsumer;

import java.util.concurrent.BlockingQueue;

public class OutputMessageConsumer extends SimpleConsumer<String> {
    public OutputMessageConsumer(BlockingQueue<String> inputQueue) {
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
