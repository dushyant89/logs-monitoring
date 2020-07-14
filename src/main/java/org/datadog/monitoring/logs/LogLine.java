package org.datadog.monitoring.logs;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class LogLine {
    private String host;

    private String user;

    private String userId;

    private ZonedDateTime dateTime;

    private String requestPath;

    private String httpMethod;

    private int statusCode;

    private String httpVersion;

    private int contentLength;

}
