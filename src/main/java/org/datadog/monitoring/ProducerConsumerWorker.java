package org.datadog.monitoring;

import java.util.concurrent.BlockingQueue;

/**
 * ProducerConsumerWorker performs work and puts the item produced in the nextQueue
 * for any worker which is waiting to consume it, thereby creating a producer-consumer relationship.
 * @param <T> The type of item to expect as input
 * @param <V> The type of item to hand over to the worker waiting on the queue
 */
public abstract class ProducerConsumerWorker<T,V> extends ConsumerWorker<T> {
    protected BlockingQueue<V> nextQueue;

    public ProducerConsumerWorker(BlockingQueue<T> inputQueue, BlockingQueue<V> nextQueue) {
        super(inputQueue);
        this.nextQueue = nextQueue;
    }

    public ProducerConsumerWorker(BlockingQueue<T> inputQueue, BlockingQueue<V> nextQueue, BlockingQueue<String> outputQueue) {
        super(inputQueue, outputQueue);
        this.nextQueue = nextQueue;
    }

    /**
     *
     * @param forNextQueue
     */
    protected void next(V forNextQueue) {
        nextQueue.offer(forNextQueue);
    }
}
