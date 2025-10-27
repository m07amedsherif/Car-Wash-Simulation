public class Semaphore {
    private int value;

    public Semaphore(int initial) {
        if (initial < 0)
            throw new IllegalArgumentException("Semaphore initial < 0");
        this.value = initial;
    }

    public synchronized void acquire() throws InterruptedException {
        while (value == 0) wait();
        value--;
    }

    public synchronized void release() {
        value++;
        notify();
    }

    public synchronized int getValue() {
        return value;
    }
}