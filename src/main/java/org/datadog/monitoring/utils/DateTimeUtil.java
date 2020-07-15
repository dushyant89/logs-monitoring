package org.datadog.monitoring.utils;

import lombok.experimental.UtilityClass;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class DateTimeUtil {

    // [13/Jul/2020:15:42:36 +0200]
    private final DateTimeFormatter dateTimeFormattter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");

    public ZonedDateTime getZonedDateTime(String inputDateTimeString) {
        return ZonedDateTime.parse(inputDateTimeString, dateTimeFormattter);
    }
}
