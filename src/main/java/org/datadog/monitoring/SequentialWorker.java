package org.datadog.monitoring;

import java.util.concurrent.BlockingQueue;

public abstract class SequentialWorker<T,V> extends SimpleWorker<T> {
    protected BlockingQueue<V> nextQueue;

    public SequentialWorker(BlockingQueue<T> inputQueue, BlockingQueue<V> nextQueue) {
        super(inputQueue);
        this.nextQueue = nextQueue;
    }

    public void next(V forNextQueue) {
        nextQueue.offer(forNextQueue);
    }
}
