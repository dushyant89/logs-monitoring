package org.datadog.monitoring;

import java.util.concurrent.BlockingQueue;

public abstract class SimpleWorker<T> implements Runnable {
    protected final BlockingQueue<T> inputQueue;
    BlockingQueue<String> outputQueue;

    public SimpleWorker(BlockingQueue<T> inputQueue) {
        this.inputQueue = inputQueue;
    }

    public SimpleWorker(BlockingQueue<T> inputQueue, BlockingQueue<String> outputQueue) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }

    /**
     *
     * @param output
     */
    protected void handOutput(String output) { outputQueue.offer(output); }
}

