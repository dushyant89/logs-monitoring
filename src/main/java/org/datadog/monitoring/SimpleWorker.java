package org.datadog.monitoring;

import java.util.concurrent.BlockingQueue;

public abstract class SimpleWorker<T> implements Runnable {
    protected final BlockingQueue<T> inputQueue;

    public SimpleWorker(BlockingQueue<T> inputQueue) {
        this.inputQueue = inputQueue;
    }
}

