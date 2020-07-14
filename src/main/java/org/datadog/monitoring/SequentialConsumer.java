package org.datadog.monitoring;

import java.util.concurrent.BlockingQueue;

public abstract class SequentialConsumer<T,V> extends SimpleConsumer<T> {
    protected BlockingQueue<V> nextQueue;

    public SequentialConsumer(BlockingQueue<T> inputQueue, BlockingQueue<V> nextQueue) {
        super(inputQueue);
        this.nextQueue = nextQueue;
    }

    public void next(V forNextQueue) {
        nextQueue.offer(forNextQueue);
    }
}
