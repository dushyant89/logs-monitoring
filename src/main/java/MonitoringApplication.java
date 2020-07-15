import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.datadog.monitoring.SequentialConsumer;
import org.datadog.monitoring.SimpleConsumer;
import org.datadog.monitoring.stats.StatsSummaryConsumer;
import org.datadog.monitoring.logs.ApacheCommonLogsParser;
import org.datadog.monitoring.logs.LogLine;
import org.datadog.monitoring.logs.LogsConsumer;
import org.datadog.monitoring.logs.LogsProducer;
import org.datadog.monitoring.logs.LogLinesConsumer;
import org.datadog.monitoring.stats.StatsSummary;
import org.datadog.monitoring.ui.OutputMessageConsumer;

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
        BlockingQueue<String> outputMessagesQueue = new LinkedBlockingQueue<>();

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

        SequentialConsumer<List<LogLine>, StatsSummary> logLinesConsumer = new LogLinesConsumer(logLinesQueue, statsSummariesQueue, outputMessagesQueue);
        Thread statsConsumerThread = new Thread(logLinesConsumer);
        statsConsumerThread.start();

        SimpleConsumer<StatsSummary> statsSummaryConsumer = new StatsSummaryConsumer(statsSummariesQueue, outputMessagesQueue, 10, 10);
        Thread alertsThread = new Thread(statsSummaryConsumer);
        alertsThread.start();

        SimpleConsumer<String> messageConsumer = new OutputMessageConsumer(outputMessagesQueue);
        Thread messagesThread = new Thread(messageConsumer);
        messagesThread.start();

        try {
            logsProducerThread.join();
            statsConsumerThread.join();
            alertsThread.join();
            messagesThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
