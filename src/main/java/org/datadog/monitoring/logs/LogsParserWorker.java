package org.datadog.monitoring.logs;

import lombok.extern.slf4j.Slf4j;
import org.datadog.monitoring.SequentialWorker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class LogsParserWorker extends SequentialWorker<String, List<LogLine>> {
    LogsParser logsParser;

    public LogsParserWorker(BlockingQueue<String> inputQueue, BlockingQueue<List<LogLine>> nextQueue, LogsParser parser) {
        super(inputQueue, nextQueue);
        this.logsParser = parser;
    }

    public void run() {
        log.trace("LogsParserWorker starting to run");

        try {
            List<String> incomingLogs = new ArrayList<>();
            // wait for the new set of incoming logs.
            incomingLogs.add(inputQueue.take());
            // Empty the queue for the next set of logs.
            inputQueue.drainTo(incomingLogs, inputQueue.size());
            // offer the parsed logs to the next queue for the next worker.
            next(parseIncomingLogs(incomingLogs));
        } catch (InterruptedException e) {
            log.warn("LogsParserWorker got interrupted", e);
        }
    }

    private List<LogLine> parseIncomingLogs(List<String> incomingLogs) {
        List<LogLine> logLines = new ArrayList<>();

        for (String incomingLog: incomingLogs) {
            try {
                logLines.add(logsParser.parseLogs(incomingLog));
            } catch (LogsParsingException e) {
                log.info(String.format("Error parsing incoming log: %s", incomingLog));
            }
        }

        return logLines;
    }
}
