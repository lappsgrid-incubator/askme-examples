package org.lappsgrid.eager.askme.prototype.workers

/**
 * The Worker class provides a lock object for implementing classes as well as start and stop methods
 */
abstract class Worker implements Runnable {
    String name = 'Worker'

    private Object lock

    Worker() {
        lock = new Object()
    }

    void block() throws InterruptedException {
        synchronized (lock) {
            lock.wait()
        }
    }

    void start() {
        new Thread(this).start()
    }

    void stop() {
        synchronized (lock) {
            lock.notify()
        }
    }
}
