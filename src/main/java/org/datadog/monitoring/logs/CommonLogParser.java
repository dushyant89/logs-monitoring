package org.datadog.monitoring.logs;

import org.datadog.monitoring.traffic.HttpMethod;
import org.datadog.monitoring.utils.DateTimeUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonLogParser implements LogsParser {
    // 249.78.222.44 - terry4451 [13/Jul/2020:15:42:36 +0200] "DELETE /syndicate/bandwidth HTTP/1.0" 403 15065
    private static final Pattern pattern = Pattern.compile("^([^\\s]+) ([^\\s]+) ([^\\s]+) \\[([\\w:/]+\\s[+\\-]\\d{4})] \"([a-zA-Z]+) ([^ \"]+) ?([^\"]+)?\" ([0-9]{3}) ([0-9]+|-)$");

    @Override
    public LogLine parseLogs(String inputLog) throws LogsParsingException {
        inputLog = inputLog.trim();

        Matcher matcher = pattern.matcher(inputLog);
        if (!matcher.matches()) {
            throw new LogsParsingException("Log format not recognizable");
        }

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
    }
}
