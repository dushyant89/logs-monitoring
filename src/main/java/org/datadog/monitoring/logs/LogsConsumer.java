package org.datadog.monitoring.logs;

import org.datadog.monitoring.SequentialConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class LogsConsumer extends SequentialConsumer<String, List<LogLine>> {
    LogsParser logsParser;

    private static final Logger logger = LoggerFactory.getLogger(LogsConsumer.class);

    public LogsConsumer(BlockingQueue<String> inputQueue, BlockingQueue<List<LogLine>> outputQueue, LogsParser parser) {
        super(inputQueue, outputQueue);
        this.logsParser = parser;
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
            // wait for the new set of incoming logs.
            incomingLogs.add(inputQueue.take());
            // Empty the queue for the next set of logs.
            inputQueue.drainTo(incomingLogs, inputQueue.size());
            // offer the parsed logs to the output queue for the next consumer.
            outputQueue.offer(parseIncomingLogs(incomingLogs));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
