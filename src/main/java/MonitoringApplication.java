import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.datadog.monitoring.SequentialConsumer;
import org.datadog.monitoring.SimpleConsumer;
import org.datadog.monitoring.alerts.AlertsConsumer;
import org.datadog.monitoring.logs.ApacheCommonLogsParser;
import org.datadog.monitoring.logs.LogLine;
import org.datadog.monitoring.logs.LogsConsumer;
import org.datadog.monitoring.logs.LogsProducer;
import org.datadog.monitoring.stats.StatsConsumer;
import org.datadog.monitoring.stats.StatsSummary;

import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.*;

public class MonitoringApplication {
    public static void main(String[] args) {
        createLogsTailerListener();
    }

    private static void createLogsTailerListener() {
        BlockingQueue<String> logsPipe = new LinkedBlockingQueue<>();
        BlockingQueue<List<LogLine>> logLinesQueue = new LinkedBlockingQueue<>();
        BlockingQueue<StatsSummary> statsSummariesQueue = new LinkedBlockingQueue<>();

        TailerListener listener = new LogsProducer(logsPipe);
        // flog -o "/tmp/access.log" -t log -d 1 -w
        // Tail the logs with minimum possible delays so that it doesn't hamper with the rate at which
        // the consumer is running.
        Tailer tailer = new Tailer(Paths.get("/tmp/access.log").toFile(), listener, 1, true);
        Thread logsProducerThread = new Thread(tailer);
        logsProducerThread.start();

        SequentialConsumer<String, List<LogLine>> logsConsumer = new LogsConsumer(logsPipe, logLinesQueue, new ApacheCommonLogsParser());
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        // Run the consumer once every x seconds.
        // and get all the logs we can in this x second timespan.
        executorService.scheduleAtFixedRate(logsConsumer, 5, 5, TimeUnit.SECONDS);

        StatsConsumer statsConsumer = new StatsConsumer(logLinesQueue, statsSummariesQueue);
        Thread statsConsumerThread = new Thread(statsConsumer);
        statsConsumerThread.start();

        SimpleConsumer<StatsSummary> alertsConsumer = new AlertsConsumer(statsSummariesQueue, 10, 10);
        Thread alertsThread = new Thread(alertsConsumer);
        alertsThread.start();

        try {
            logsProducerThread.join();
            statsConsumerThread.join();
            alertsThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
