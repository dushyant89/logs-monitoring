import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
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

import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.*;

public class MonitoringApplication {
    public static void main(String[] args) {
        createLogsTailerListener();
    }

    private static void createLogsTailerListener() {
        BlockingQueue<String> incomingLogsQueue = new LinkedBlockingQueue<>();
        BlockingQueue<List<LogLine>> logLinesQueue = new LinkedBlockingQueue<>();
        BlockingQueue<StatsSummary> statsSummariesQueue = new LinkedBlockingQueue<>();
        BlockingQueue<String> messagesQueue = new LinkedBlockingQueue<>();

        TailerListener listener = new LogsListener(incomingLogsQueue);
        // flog -o "/tmp/access.log" -t log -d 1 -w
        // Tail the logs with minimum possible delays so that it doesn't hamper with the rate at which
        // the consumer is running.
        Tailer tailer = new Tailer(Paths.get("/tmp/access.log").toFile(), listener, 1, true);
        Thread logsProducerThread = new Thread(tailer);
        logsProducerThread.start();

        SequentialWorker<String, List<LogLine>> logsWorker = new LogsWorker(incomingLogsQueue, logLinesQueue, new ApacheCommonLogsParser());
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        // Run the consumer once every x seconds.
        // and get all the logs we can in this x second timespan.
        executorService.scheduleAtFixedRate(logsWorker, 5, 5, TimeUnit.SECONDS);

        SequentialWorker<List<LogLine>, StatsSummary> logLinesWorker = new LogLinesWorker(logLinesQueue, statsSummariesQueue, messagesQueue);
        Thread statsConsumerThread = new Thread(logLinesWorker);
        statsConsumerThread.start();

        SimpleWorker<StatsSummary> statsSummaryWorker = new StatsSummaryWorker(statsSummariesQueue, messagesQueue, 10, 10);
        Thread alertsThread = new Thread(statsSummaryWorker);
        alertsThread.start();

        SimpleWorker<String> messageWorker = new PrintMessageWorker(messagesQueue);
        Thread messagesThread = new Thread(messageWorker);
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
