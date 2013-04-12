package org.shunya.dli;

public class Tap {
    private boolean block = false;

    public synchronized void await() throws InterruptedException {
        while (block) {
            wait();
        }
    }

    public synchronized void on() {
        block = false;
        notifyAll();
    }

    public synchronized void off() {
        block = true;
        notifyAll();
    }

}
