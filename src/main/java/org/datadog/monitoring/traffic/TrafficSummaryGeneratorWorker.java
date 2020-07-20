package org.datadog.monitoring.traffic;

import lombok.extern.slf4j.Slf4j;
import org.datadog.monitoring.ProducerConsumerWorker;
import org.datadog.monitoring.logs.LogLine;

import java.util.List;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class TrafficSummaryGeneratorWorker extends ProducerConsumerWorker<List<LogLine>, TrafficSummary> {

    public TrafficSummaryGeneratorWorker(BlockingQueue<List<LogLine>> inputQueue, BlockingQueue<TrafficSummary> nextQueue, BlockingQueue<String> outputQueue) {
        super(inputQueue, nextQueue, outputQueue);
    }

    public void run() {
        log.trace("TrafficSummaryGeneratorWorker starting to run");

        while (true) {
            try {
                TrafficSummary trafficSummary = prepareTrafficSummary(inputQueue.take());
                handOutput(trafficSummary.toString());
                next(trafficSummary);
            } catch (InterruptedException e) {
                log.warn("TrafficSummaryGeneratorWorker got interrupted", e);
            }
        }
    }

    private TrafficSummary prepareTrafficSummary(List<LogLine> logLines) {
        TrafficSummary trafficSummary = new TrafficSummary();

        for (LogLine logLine: logLines) {
            trafficSummary.incrementRequestCount();
            trafficSummary.incrementRequestContentSize(logLine.getContentLength());

            String [] sectionParts = logLine.getRequestPath().split("/");
            String section = "/";

            if (sectionParts.length > 1) {
                section = sectionParts[1];
            }

            trafficSummary.getHttpMethodsWiseHits().compute(logLine.getHttpMethod(), (k, v) -> v == null ? 1: v+1);

            trafficSummary.getSectionWiseHits().compute(section, (k, v) -> v == null ? 1: v+1);
        }

        return trafficSummary;
    }
}
