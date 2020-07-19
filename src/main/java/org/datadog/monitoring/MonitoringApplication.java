package org.datadog.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.Tailer;
import org.datadog.monitoring.stats.StatsSummaryWorker;
import org.datadog.monitoring.logs.ApacheCommonLogsParser;
import org.datadog.monitoring.logs.LogLine;
import org.datadog.monitoring.logs.LogsWorker;
import org.datadog.monitoring.logs.LogsListener;
import org.datadog.monitoring.logs.LogLinesWorker;
import org.datadog.monitoring.stats.StatsSummary;
import org.datadog.monitoring.ui.PrintMessageWorker;
import org.datadog.monitoring.utils.ApplicationUtil;

import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Slf4j
public class MonitoringApplication {
    public static void main(String[] args) {
        Optional<ApplicationConfig> applicationConfigOptional = ApplicationUtil.validateUserProvidedArgs(args);
        if (applicationConfigOptional.isEmpty()) {
            System.exit(1);
        }

        new MonitoringApplication().startWorkers(applicationConfigOptional.get());
    }

    private void startWorkers(ApplicationConfig appConfig) {
        log.trace("starting workers");

        BlockingQueue<String> incomingLogsQueue = new LinkedBlockingQueue<>();
        BlockingQueue<List<LogLine>> logLinesQueue = new LinkedBlockingQueue<>();
        BlockingQueue<StatsSummary> statsSummariesQueue = new LinkedBlockingQueue<>();
        BlockingQueue<String> messagesQueue = new LinkedBlockingQueue<>();

        // These executors will not be shutdown, unless the user closes the application or the main thread dies.
        ScheduledExecutorService scheduledLogsWorker = Executors.newSingleThreadScheduledExecutor();
        ExecutorService executableWorkers = Executors.newFixedThreadPool(4);

        try {
            scheduledLogsWorker.scheduleAtFixedRate(
                    new LogsWorker(incomingLogsQueue, logLinesQueue, new ApacheCommonLogsParser()),
                    appConfig.getStatsDisplayInterval(),
                    appConfig.getStatsDisplayInterval(),
                    TimeUnit.SECONDS
            );

            // Tail the logs with minimum possible delays so that it doesn't hamper with the rate at which
            // the consumer is running.
            executableWorkers.submit(new Tailer(
                    Paths.get(appConfig.getLogFileLocation()).toFile(),
                    new LogsListener(incomingLogsQueue),
                    0,
                    true)
            );
            executableWorkers.submit(new LogLinesWorker(logLinesQueue, statsSummariesQueue, messagesQueue));
            executableWorkers.submit(new StatsSummaryWorker(
                    statsSummariesQueue,
                    messagesQueue,
                    appConfig.getAlertsMonitoringInterval() / appConfig.getStatsDisplayInterval(),
                    appConfig.getRequestsPerSecondThreshold()
            ));
            executableWorkers.submit(new PrintMessageWorker(messagesQueue));
        } catch (RejectedExecutionException executionException) {
            log.error("Could not start one of the workers, run the application again", executionException);
        }
    }
}
