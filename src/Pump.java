import java.util.concurrent.ThreadLocalRandom;

public class Pump extends Thread {
    private final int pumpId;
    private final ServiceStation station;

    public Pump(int pumpId, ServiceStation station) {
        this.pumpId = pumpId;
        this.station = station;
    }

    @Override
    public void run() {
        try {
            while (true) {
                synchronized (station.countLock) {
                    if (station.totalCarsProcessed >= station.totalCars)
                        break; // all cars processed
                }

                Car car = station.dequeue();

                synchronized (station.countLock) {
                    if (station.totalCarsProcessed >= station.totalCars)
                        break;
                    station.totalCarsProcessed++;
                }

                ServiceStation.log("• Pump " + pumpId + ": " + car.name + " Occupied");
                int bayId = station.occupyBay();

                ServiceStation.log("• Pump " + pumpId + ": " + car.name + " login");
                ServiceStation.log("• Pump " + pumpId + ": " + car.name + " begins service at Bay " + bayId);

                Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 2500));

                ServiceStation.log("• Pump " + pumpId + ": " + car.name + " finishes service");
                station.releaseBay(bayId);
                ServiceStation.log("• Pump " + pumpId + ": Bay " + bayId + " is now free");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
