import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.datadog.monitoring.logs.LogsTailerListener;

public class MonitoringApplication {
    public static void main(String[] args) {
        TailerListener listener = new LogsTailerListener();
        Tailer tailer = new Tailer(file, listener, 500);
        Thread thread = new Thread(tailer);
        thread.setDaemon(true); // optional
        thread.start();
    }
}
