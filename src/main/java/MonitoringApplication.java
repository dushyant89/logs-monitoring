import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.datadog.monitoring.logs.ApacheCommonLogsParser;
import org.datadog.monitoring.logs.LogLine;
import org.datadog.monitoring.logs.consumer.LogsConsumer;
import org.datadog.monitoring.logs.producer.LogsTailerListener;
import org.datadog.monitoring.stats.StatsConsumer;

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

        TailerListener listener = new LogsTailerListener(logsPipe);
        // flog -o "/tmp/access.log" -t log -d 1 -w
        // Tail the logs with minimum possible delays so that it doesn't hamper with the rate at which
        // the consumer is running.
        Tailer tailer = new Tailer(Paths.get("/tmp/access.log").toFile(), listener, 1, true);
        Thread logsProducerThread = new Thread(tailer);
        logsProducerThread.start();

        LogsConsumer logsConsumer = new LogsConsumer(logsPipe, logLinesQueue, new ApacheCommonLogsParser());
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        // Run the consumer once every x seconds.
        // and get all the logs we can in this x second timespan.
        executorService.scheduleAtFixedRate(logsConsumer, 10, 10, TimeUnit.SECONDS);

        StatsConsumer statsConsumer = new StatsConsumer(logLinesQueue);
        Thread statsConsumerThread = new Thread(statsConsumer);
        statsConsumerThread.start();

        try {
            logsProducerThread.join();
            statsConsumerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
