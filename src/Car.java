public class Car extends Thread {
    public final String name;
    private final ServiceStation station;

    public Car(String name, ServiceStation station) {
        this.name = name;
        this.station = station;
    }

    @Override
    public void run() {
        ServiceStation.log("• " + name + " arrived");
        station.enqueue(this);
    }
}