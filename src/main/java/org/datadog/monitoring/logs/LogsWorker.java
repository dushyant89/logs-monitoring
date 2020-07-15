package org.datadog.monitoring.logs;

import org.datadog.monitoring.SequentialWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class LogsWorker extends SequentialWorker<String, List<LogLine>> {
    LogsParser logsParser;

    private static final Logger logger = LoggerFactory.getLogger(LogsWorker.class);

    public LogsWorker(BlockingQueue<String> inputQueue, BlockingQueue<List<LogLine>> nextQueue, LogsParser parser) {
        super(inputQueue, nextQueue);
        this.logsParser = parser;
    }

    public void run() {
        try {
            List<String> incomingLogs = new ArrayList<>();
            // wait for the new set of incoming logs.
            incomingLogs.add(inputQueue.take());
            // Empty the queue for the next set of logs.
            inputQueue.drainTo(incomingLogs, inputQueue.size());
            // offer the parsed logs to the output queue for the next worker.
            next(parseIncomingLogs(incomingLogs));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
}
