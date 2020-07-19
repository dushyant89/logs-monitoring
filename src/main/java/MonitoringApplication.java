import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.datadog.monitoring.ApplicationConfig;
import org.datadog.monitoring.SequentialWorker;
import org.datadog.monitoring.SimpleWorker;
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
    /**
     *
     */
    Tailer tailer;
    /**
     *
     */
    SequentialWorker<String, List<LogLine>> logsWorker;
    /**
     *
     */
    SequentialWorker<List<LogLine>, StatsSummary> logLinesWorker;
    /**
     *
     */
    SimpleWorker<StatsSummary> statsSummaryWorker;
    /**
     *
     */
    SimpleWorker<String> messageWorker;

    public static void main(String[] args) {
        Optional<ApplicationConfig> applicationConfigOptional = ApplicationUtil.validateUserProvidedArgs(args);
        if (applicationConfigOptional.isEmpty()) {
            System.exit(1);
        }

        MonitoringApplication  application = new MonitoringApplication();

        application.setupWorkers(applicationConfigOptional.get());
        application.startWorkers(applicationConfigOptional.get());
    }

    private void setupWorkers(ApplicationConfig appConfig) {
        log.info("starting workers");
        BlockingQueue<String> incomingLogsQueue = new LinkedBlockingQueue<>();
        BlockingQueue<List<LogLine>> logLinesQueue = new LinkedBlockingQueue<>();
        BlockingQueue<StatsSummary> statsSummariesQueue = new LinkedBlockingQueue<>();
        BlockingQueue<String> messagesQueue = new LinkedBlockingQueue<>();

        TailerListener listener = new LogsListener(incomingLogsQueue);

        // Tail the logs with minimum possible delays so that it doesn't hamper with the rate at which
        // the consumer is running.
        tailer = new Tailer(Paths.get(appConfig.getLogFileLocation()).toFile(), listener, 1, true);

        logsWorker = new LogsWorker(incomingLogsQueue, logLinesQueue, new ApacheCommonLogsParser());

        logLinesWorker = new LogLinesWorker(logLinesQueue, statsSummariesQueue, messagesQueue);

        statsSummaryWorker = new StatsSummaryWorker(
                statsSummariesQueue,
                messagesQueue,
                appConfig.getAlertsMonitoringInterval() / appConfig.getStatsDisplayInterval(),
                appConfig.getRequestsPerSecondThreshold()
        );

        messageWorker = new PrintMessageWorker(messagesQueue);
    }

    private void startWorkers(ApplicationConfig appConfig) {
        Thread logsProducerThread = new Thread(tailer);

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        executorService.scheduleAtFixedRate(logsWorker, appConfig.getStatsDisplayInterval(), appConfig.getStatsDisplayInterval(), TimeUnit.SECONDS);

        Thread statsConsumerThread = new Thread(logLinesWorker);

        Thread alertsThread = new Thread(statsSummaryWorker);

        Thread messagesThread = new Thread(messageWorker);

        logsProducerThread.start();
        statsConsumerThread.start();
        alertsThread.start();
        messagesThread.start();
        try {
            logsProducerThread.join();
            statsConsumerThread.join();
            alertsThread.join();
            messagesThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executorService.shutdown();
    }
}
