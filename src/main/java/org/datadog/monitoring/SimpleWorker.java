package org.datadog.monitoring;

import java.util.concurrent.BlockingQueue;

public abstract class SimpleWorker<T> implements Runnable {
    protected final BlockingQueue<T> inputQueue;
    protected BlockingQueue<String> messageQueue;

    public SimpleWorker(BlockingQueue<T> inputQueue) {
        this.inputQueue = inputQueue;
    }

    public SimpleWorker(BlockingQueue<T> inputQueue, BlockingQueue<String> messageQueue) {
        this.inputQueue = inputQueue;
        this.messageQueue = messageQueue;
    }

    /**
     *
     * @param output
     */
    protected void handOutput(String output) { messageQueue.offer(output); }
}

