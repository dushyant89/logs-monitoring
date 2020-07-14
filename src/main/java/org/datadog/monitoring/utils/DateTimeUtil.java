package org.datadog.monitoring.utils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtil {
    private DateTimeUtil() {} // util

    // [13/Jul/2020:15:42:36 +0200]
    private static final DateTimeFormatter dateTimeFormattter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");

    public static ZonedDateTime getZonedDateTime(String inputDateTimeString) {
        return ZonedDateTime.parse(inputDateTimeString, dateTimeFormattter);
    }
}
