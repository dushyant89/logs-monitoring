package org.datadog.monitoring.logs;

import lombok.extern.slf4j.Slf4j;
import org.datadog.monitoring.stats.HttpMethod;
import org.datadog.monitoring.utils.DateTimeUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ApacheCommonLogsParser implements LogsParser {
    // 249.78.222.44 - terry4451 [13/Jul/2020:15:42:36 +0200] "DELETE /syndicate/bandwidth HTTP/1.0" 403 15065
    // @TODO: pattern
    private static final Pattern pattern = Pattern.compile("^(\\S+) (\\S+) (\\S+) \\[([^]]+)] \"([A-Z]+) ([^ \"]+) ?([^\"]+)?\" ([0-9]{3}) ([0-9]+|-)$");

    public LogLine parseLogs(String inputLog) throws LogsParsingException {
        Matcher matcher = pattern.matcher(inputLog);
        if (!matcher.matches()) {
            throw new LogsParsingException("Log format not recognizable");
        }

        try {
            return LogLine
                    .builder()
                    .host(matcher.group(1))
                    .user(matcher.group(2))
                    .userId(matcher.group(3))
                    .dateTime(DateTimeUtil.getZonedDateTime(matcher.group(4)))
                    .httpMethod(HttpMethod.getHttpMethod(matcher.group(5)).orElse(HttpMethod.OTHER))
                    .requestPath(matcher.group(6))
                    .httpVersion(matcher.group(7))
                    .statusCode(Integer.parseInt(matcher.group(8)))
                    .contentLength(Integer.parseInt(matcher.group(9)))
                    .build();
        } catch (Exception e) {
            log.info(String.format("Error parsing log line: %s", e.getMessage()));
        }

        throw new LogsParsingException("Invalid log format");
    }
}
