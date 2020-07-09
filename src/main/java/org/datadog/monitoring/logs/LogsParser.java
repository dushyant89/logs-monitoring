package org.datadog.monitoring.logs;

import java.util.concurrent.BlockingQueue;

public class LogsParser implements Runnable {
    BlockingQueue<String> logsConsumer;

    public LogsParser(BlockingQueue<String> logsConsumer) {
        this.logsConsumer = logsConsumer;
    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println(logsConsumer.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
