package org.datadog.monitoring;

import lombok.Data;

import java.util.concurrent.BlockingQueue;

@Data
public abstract class SequentialConsumer<T,V> implements Runnable {
    protected BlockingQueue<T> inputQueue;
    protected BlockingQueue<V> outputQueue;

    public SequentialConsumer(BlockingQueue<T> inputQueue, BlockingQueue<V> outputQueue) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }
}
