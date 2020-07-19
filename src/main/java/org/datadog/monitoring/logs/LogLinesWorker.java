package org.datadog.monitoring.logs;

import lombok.extern.slf4j.Slf4j;
import org.datadog.monitoring.SequentialWorker;
import org.datadog.monitoring.stats.StatsSummary;

import java.util.List;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class LogLinesWorker extends SequentialWorker<List<LogLine>, StatsSummary> {

    public LogLinesWorker(BlockingQueue<List<LogLine>> inputQueue, BlockingQueue<StatsSummary> nextQueue, BlockingQueue<String> outputQueue) {
        super(inputQueue, nextQueue, outputQueue);
    }

    public void run() {
        log.trace("LogLinesWorker starting to run");

        while (true) {
            try {
                StatsSummary statsSummary = prepareStatsSummary(inputQueue.take());
                handOutput(statsSummary.toString());
                next(statsSummary);
            } catch (InterruptedException e) {
                log.warn("LogLinesWorker got interrupted", e);
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
