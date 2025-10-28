import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ServiceStation {
    private final LinkedList<Car> queue = new LinkedList<>();
    private final Semaphore empty;
    private final Semaphore full;
    private final Semaphore mutex;
    // the mutex is for preventing that more than one car enters the queue at the same time
    private final Semaphore bays;
    private final Object bayLock = new Object();
    private final boolean[] bayOccupied;
    private final int pumpsCount;
    private final List<Pump> pumps = new ArrayList<>();

    // tracking total cars
    protected int totalCarsProcessed = 0;
    protected final int totalCars;
    protected final Object countLock = new Object();

    public ServiceStation(int waitingCapacity, int pumpsCount, int totalCars) {
        this.empty = new Semaphore(waitingCapacity);
        this.full = new Semaphore(0);
        this.mutex = new Semaphore(1);
        this.bays = new Semaphore(pumpsCount);
        this.pumpsCount = pumpsCount;
        this.bayOccupied = new boolean[pumpsCount];
        this.totalCars = totalCars;
    }

    public void startPumps() {
        for (int i = 0; i < pumpsCount; i++) {
            Pump p = new Pump(i + 1, this);
            pumps.add(p);
            p.start();
        }
    }

    public void enqueue(Car car) {
        try {
            boolean hadToWait = false;
            if (bays.getValue() == 0) hadToWait = true;

            empty.acquire();
            mutex.acquire();
            //to prevent more than one car to enter the queue at the same time
            queue.addLast(car);

            if (hadToWait)
                log("• " + car.name + " arrived and waiting");
            else
                log("• " + car.name + " entered the queue" );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
            full.release();
        }
    }

    public Car dequeue() throws InterruptedException {
        full.acquire();
        mutex.acquire();
        Car car;
        try {
            car = queue.removeFirst();
        } finally {
            mutex.release();
            empty.release();
        }
        return car;
    }

    public int occupyBay() throws InterruptedException {
        bays.acquire();
        synchronized (bayLock) {
            for (int i = 0; i < bayOccupied.length; i++) {
                if (!bayOccupied[i]) {
                    bayOccupied[i] = true;
                    return i + 1;
                }
            }
        }
        return -1;
    }

    public void releaseBay(int bayId) {
        synchronized (bayLock) {
            bayOccupied[bayId - 1] = false;
        }
        bays.release();
    }

    public static void log(String s) {
        System.out.println(s);
    }

    public static void main(String[] args) throws InterruptedException {
        int waitingCapacity = 5;
        int pumps = 3;
        List<String> carNames = Arrays.asList("C1", "C2", "C3", "C4", "C5", "C6", "C7");

        ServiceStation station = new ServiceStation(waitingCapacity, pumps, carNames.size());
        station.startPumps();

        List<Car> created = new ArrayList<>();
        for (String name : carNames) {
            Car c = new Car(name, station);
            created.add(c);
            c.start();
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 400));
        }

        for (Car c : created) c.join();
        for (Pump p : station.pumps) p.join();

        log("• All cars processed; simulation ends");
    }
}