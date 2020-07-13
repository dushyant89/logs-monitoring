package org.datadog.monitoring.stats;

import org.datadog.monitoring.logs.LogLine;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class StatsConsumer implements Runnable {
    BlockingQueue<List<LogLine>> logLinesQueue;

    public StatsConsumer(BlockingQueue<List<LogLine>> logLinesQueue) {
        this.logLinesQueue = logLinesQueue;
    }

    public void run() {
        while (true) {
            try {
                List<LogLine> logLines = logLinesQueue.take();
                StatsSummary statsSummary = new StatsSummary();

                for (LogLine logLine: logLines) {
                    statsSummary.incrementRequestCount();
                    statsSummary.incrementRequestContentSize(logLine.getContentLength());

                    String [] sectionParts = logLine.getRequestPath().split("/");
                    String section = "/";

                    if (sectionParts.length > 1) {
                        section = sectionParts[1];
                    }

                    statsSummary.getSectionCount().compute(section, (k,v) -> {
                        if (v == null) {
                            return 1;
                        }

                        return v + 1;
                    });
                }

                statsSummary.print();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
