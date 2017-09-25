import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OperationsService {
    private static final int WAIT_SEC = 1; //time in seconds to wait to lock

    public static void main(String[] args) {
        final Account a = new Account(1000);
        final Account b = new Account(2000);

        ScheduledExecutorService monitor = createMonitoringService(a, 3, 2);

        ExecutorService service = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 10; i++)
            service.submit(new Transfer(a, b, new Random().nextInt(200)));

        service.shutdown();
        try {
            service.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

        monitor.shutdown();
    }

    public static ScheduledExecutorService createMonitoringService(
            Account acc, int initialDelaySec, int periodSec) {
        ScheduledExecutorService monitoringService = Executors.newScheduledThreadPool(1);
        monitoringService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("Failed tranfers at " + acc.toString() + ": " + acc.getFailCounter());
            }
        }, initialDelaySec, periodSec, TimeUnit.SECONDS);

        return monitoringService;
    }
}
