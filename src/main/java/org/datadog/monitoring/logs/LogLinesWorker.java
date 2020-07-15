package org.datadog.monitoring.logs;

import org.datadog.monitoring.SequentialWorker;
import org.datadog.monitoring.stats.StatsSummary;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class LogLinesWorker extends SequentialWorker<List<LogLine>, StatsSummary> {
    BlockingQueue<String> outputQueue;

    public LogLinesWorker(BlockingQueue<List<LogLine>> inputQueue, BlockingQueue<StatsSummary> nextQueue, BlockingQueue<String> outputQueue) {
        super(inputQueue, nextQueue);
        this.outputQueue = outputQueue;
    }

    public void run() {
        while (true) {
            try {
                StatsSummary statsSummary = prepareStatsSummary(inputQueue.take());
                outputQueue.offer(statsSummary.toString());
                // offer the summary for the next worker.
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

            statsSummary.getHttpMethodsWiseHits().compute(logLine.getHttpMethod(), (k,v) -> v == null ? 1: v+1);

            statsSummary.getSectionWiseHits().compute(section, (k,v) -> v == null ? 1: v+1);
        }

        return statsSummary;
    }
}
