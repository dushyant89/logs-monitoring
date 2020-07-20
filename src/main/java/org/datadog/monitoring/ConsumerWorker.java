package org.datadog.monitoring;

import java.util.concurrent.BlockingQueue;

/**
 * ConsumerWorker waits to consume the item in the `inputQueue` and performs it work.
 */
public abstract class ConsumerWorker<T> implements Runnable {
    // Contains the item required by the worker to function.
    protected final BlockingQueue<T> inputQueue;
    // Queue for sending out any output message produced by the worker.
    protected BlockingQueue<String> messageQueue;

    public ConsumerWorker(BlockingQueue<T> inputQueue) {
        this.inputQueue = inputQueue;
    }

    public ConsumerWorker(BlockingQueue<T> inputQueue, BlockingQueue<String> messageQueue) {
        this.inputQueue = inputQueue;
        this.messageQueue = messageQueue;
    }

    /**
     * Adds the output to the message queue. The output can be consumed by
     * any worker which is waiting for an item in the message queue.
     * @param output
     */
    protected void handOutput(String output) { messageQueue.offer(output); }
}

