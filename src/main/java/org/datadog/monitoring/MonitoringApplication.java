package org.datadog.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.Tailer;
import org.datadog.monitoring.alerts.HighTrafficAlertsMonitor;
import org.datadog.monitoring.alerts.AlertsMonitorWorker;
import org.datadog.monitoring.config.ApplicationConfig;
import org.datadog.monitoring.logs.ApacheCommonLogsParser;
import org.datadog.monitoring.logs.LogLine;
import org.datadog.monitoring.logs.LogsParserWorker;
import org.datadog.monitoring.logs.LogsListener;
import org.datadog.monitoring.traffic.TrafficSummaryGeneratorWorker;
import org.datadog.monitoring.traffic.TrafficSummary;
import org.datadog.monitoring.ui.PrintMessageWorker;
import org.datadog.monitoring.utils.ApplicationConfigUtil;

import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Slf4j
public class MonitoringApplication {
    public static void main(String[] args) {
        Optional<ApplicationConfig> applicationConfigOptional = ApplicationConfigUtil.validateUserProvidedArgs(args);
        if (applicationConfigOptional.isEmpty()) {
            System.exit(1);
        }

        startWorkers(applicationConfigOptional.get());
    }

    private static void startWorkers(ApplicationConfig appConfig) {
        log.trace("starting workers");

        BlockingQueue<String> incomingLogsQueue = new LinkedBlockingQueue<>();
        BlockingQueue<List<LogLine>> logLinesQueue = new LinkedBlockingQueue<>();
        BlockingQueue<TrafficSummary> trafficSummaryQueue = new LinkedBlockingQueue<>();
        BlockingQueue<String> messagesQueue = new LinkedBlockingQueue<>();

        // These executors will not be shutdown, unless the user closes the application or the main thread dies.
        // If due to some reason any of the executors gets interrupted, they will be restarted.
        ScheduledExecutorService scheduledLogsWorker = Executors.newSingleThreadScheduledExecutor();
        ExecutorService executableWorkers = Executors.newFixedThreadPool(4);

        try {
            // scheduled worker for receiving tailed logs and parsing them.
            scheduledLogsWorker.scheduleAtFixedRate(
                    new LogsParserWorker(incomingLogsQueue, logLinesQueue, new ApacheCommonLogsParser()),
                    appConfig.getStatsDisplayInterval(),
                    appConfig.getStatsDisplayInterval(),
                    TimeUnit.SECONDS
            );
            // worker which tails the log file for any new events.
            executableWorkers.submit(new Tailer(
                    Paths.get(appConfig.getLogFileLocation()).toFile(),
                    new LogsListener(incomingLogsQueue),
                    0,
                    true)
            );
            // worker consuming parsed log lines and produces the traffic summary for the log lines received.
            executableWorkers.submit(new TrafficSummaryGeneratorWorker(
                    logLinesQueue,
                    trafficSummaryQueue,
                    messagesQueue)
            );
            // worker which consumes traffic summary and monitors for any possible alerts.
            executableWorkers.submit(new AlertsMonitorWorker(
                    trafficSummaryQueue,
                    messagesQueue,
                    new HighTrafficAlertsMonitor(
                            appConfig.getAlertsMonitoringInterval() / appConfig.getStatsDisplayInterval(),
                            appConfig.getRequestsPerSecondThreshold()
                    )
            ));
            executableWorkers.submit(new PrintMessageWorker(messagesQueue));
        } catch (RejectedExecutionException executionException) {
            log.error("Could not start one of the workers, run the application again", executionException);
        }
    }
}
