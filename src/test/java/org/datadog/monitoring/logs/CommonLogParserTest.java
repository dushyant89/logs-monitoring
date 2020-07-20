package org.datadog.monitoring.logs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommonLogParserTest {
    private LogsParser logsParser;

    @BeforeAll
    public void setLogsParser() {
        logsParser = new CommonLogParser();
    }

    @Test
    public void testInvalidLogFormats() {
        // given
        final String [] invalidLogLines = new String[] {
                "249.78.222.44 - terry4451 \"DELETE /syndicate/bandwidth HTTP/1.0\" 403 15065", // no timestamp
                "249.78.222.44 - terry4451 [13/Jul/2020:15:42:36 +0200] 403 15065", // no request line
                "249.78.222.44 - terry4451 [13/Jul/2020:15:42:36 +0200] \"PUT /syndicate/bandwidth HTTP/1.0\" 200", // no content size
                "249.78.222.44 - terry4451 [13/Jul/2020:15:42:36 +0200] \"PUT /syndicate/bandwidth HTTP/1.0\" 2001 15065", // invalid response code
                "249.78.222.44 - terry4451 [13/Jul/2020:15:42:36 +0200] \"PUT /syndicate/bandwidth HTTP/1.0\" 200 1MB" // stringified content size
        };

        for (String invalidLogLine: invalidLogLines) {
            Assertions.assertThrows(LogsParsingException.class, () -> logsParser.parseLogs(invalidLogLine));
        }
    }

    @Test
    public void testValidLogFormats() {
        // given
        final String [] validLogLines = new String[] {
                "249.78.222.44 - terry4451 [13/Jul/2020:15:42:36 +0200] \"PUT /syndicate/bandwidth HTTP/1.0\" 200 15065", // no id check
                "249.78.222.44 - - [13/Jul/2020:15:42:36 +0200] \"PUT /syndicate/bandwidth HTTP/1.0\" 200 15065", // no user id
                " 249.78.222.44 - terry4451 [13/Jul/2020:15:42:36 +0200] \"GET / HTTP/1.0\" 200 15065 " // index section in request path
        };

        for (String validLogLine: validLogLines) {
            Assertions.assertDoesNotThrow(() -> logsParser.parseLogs(validLogLine));
        }
    }
}
