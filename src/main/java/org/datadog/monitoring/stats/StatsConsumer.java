package org.datadog.monitoring.stats;

import org.datadog.monitoring.SequentialConsumer;
import org.datadog.monitoring.logs.LogLine;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class StatsConsumer extends SequentialConsumer<List<LogLine>, StatsSummary> {
    BlockingQueue<String> outputQueue;

    public StatsConsumer(BlockingQueue<List<LogLine>> inputQueue, BlockingQueue<StatsSummary> nextQueue, BlockingQueue<String> outputQueue) {
        super(inputQueue, nextQueue);
        this.outputQueue = outputQueue;
    }

    public void run() {
        while (true) {
            try {
                StatsSummary statsSummary = prepareStatsSummary(inputQueue.take());
                outputQueue.offer(statsSummary.toString());
                // offer the summary for the next consumer.
                next(statsSummary);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private StatsSummary prepareStatsSummary(List<LogLine> logLines) {
        StatsSummary statsSummary = new StatsSummary();

        for (LogLine logLine: logLines) {
            statsSummary.incrementRequestCount();
            statsSummary.incrementRequestContentSize(logLine.getContentLength());

            String [] sectionParts = logLine.getRequestPath().split("/");
            String section = "/";

            if (sectionParts.length > 1) {
                section = sectionParts[1];
            }

            statsSummary.getSectionWiseHits().compute(section, (k,v) -> {
                if (v == null) {
                    return 1;
                }

                return v + 1;
            });
        }

        return statsSummary;
    }
}
