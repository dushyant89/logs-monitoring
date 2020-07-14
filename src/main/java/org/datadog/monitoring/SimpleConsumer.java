package org.datadog.monitoring;

import java.util.concurrent.BlockingQueue;

public abstract class SimpleConsumer<T> implements Runnable {
    protected final BlockingQueue<T> inputQueue;

    public SimpleConsumer(BlockingQueue<T> inputQueue) {
        this.inputQueue = inputQueue;
    }
}

