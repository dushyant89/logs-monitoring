package org.datadog.monitoring.logs;

public interface LogsParser {
    // @TODO: throw exception
    LogLine parseLogs(String log) throws LogsParsingException;
}
