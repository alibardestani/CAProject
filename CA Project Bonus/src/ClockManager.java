public class ClockManager {
    private int clockCycle = 0;
    private final int totalCores;
    private int reportCount = 0;

    public ClockManager(int totalCores) {
        this.totalCores = totalCores;
    }

    public synchronized void incrementAndReport(int coreId) {
        reportCount++;
        if (reportCount == totalCores) {
            System.out.println("Clock Cycle: " + clockCycle + " [All Cores have reported]");
            clockCycle++;
            reportCount = 0;
            notifyAll();
        } else {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
