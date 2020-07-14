package org.datadog.monitoring.logs.consumer;

import org.datadog.monitoring.logs.LogLine;
import org.datadog.monitoring.logs.LogsParser;
import org.datadog.monitoring.logs.LogsParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class LogsConsumer implements Runnable {
    BlockingQueue<String> logsQueue;
    BlockingQueue<List<LogLine>> logLinesQueue;
    LogsParser logsParser;

    private static final Logger logger = LoggerFactory.getLogger(LogsConsumer.class);

    public LogsConsumer(BlockingQueue<String> logsQueue, BlockingQueue<List<LogLine>> logLinesQueue, LogsParser parser) {
        this.logsQueue = logsQueue;
        this.logsParser = parser;
        this.logLinesQueue = logLinesQueue;
    }

    private List<LogLine> parseIncomingLogs(List<String> incomingLogs) {
        List<LogLine> logLines = new ArrayList<>();

        for (String incomingLog: incomingLogs) {
            try {
                logLines.add(logsParser.parseLogs(incomingLog));
            } catch (LogsParsingException e) {
                logger.info(String.format("Error parsing incoming log: %s with error: %s", incomingLog, e.getMessage()));
            }
        }

        return logLines;
    }

    public void run() {
        try {
            List<String> incomingLogs = new ArrayList<>();
            incomingLogs.add(logsQueue.take());
            // Empty the queue for the next set of logs.
            logsQueue.drainTo(incomingLogs, logsQueue.size());

            logLinesQueue.offer(parseIncomingLogs(incomingLogs));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
