import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.datadog.monitoring.logs.LogsParser;
import org.datadog.monitoring.logs.LogsTailerListener;

import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MonitoringApplication {
    public static void main(String[] args) {
        createLogsTailerListener();
    }

    private static void createLogsTailerListener() {
        BlockingQueue<String> logsPipe = new LinkedBlockingQueue<>();

        TailerListener listener = new LogsTailerListener(logsPipe);
        Tailer tailer = new Tailer(Paths.get("/var/log/system.log").toFile(), listener, 500, true);
        Thread producerThread = new Thread(tailer);
        producerThread.setDaemon(true); // optional
        producerThread.start();

        LogsParser logsParser = new LogsParser(logsPipe);
        Thread consumerThread = new Thread(logsParser);
        consumerThread.setDaemon(true);
        consumerThread.start();

        try {
            producerThread.join();
            consumerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
