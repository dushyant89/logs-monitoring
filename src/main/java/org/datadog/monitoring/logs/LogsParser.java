package org.datadog.monitoring.logs;

public interface LogsParser {
    /**
     * Parse the incoming log lines
     * @param log String representing the log line
     * @return parsed LogLine
     * @throws LogsParsingException
     */
    LogLine parseLogs(String log) throws LogsParsingException;
}
