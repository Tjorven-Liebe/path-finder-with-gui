package de.tjorven.pathfinder.gui;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AsyncTickService {

    private final List<Runnable> registeredRunnables = new CopyOnWriteArrayList<>();
    private Thread mainThread;

    public void register(Runnable runnable) {
        registeredRunnables.add(runnable);
    }

    public void unregister(Runnable runnable) {
        registeredRunnables.remove(runnable);
    }

    public void startTicker() {
        if (mainThread != null && mainThread.isAlive()) {
            throw new IllegalStateException("Ticker is already running");
        }

        mainThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(50); // 20 Ticks = 1 Second
                    for (Runnable runnable : registeredRunnables) {
                        runnable.run();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupt status and exit
                    break;
                }
            }
        });

        mainThread.setDaemon(true); // Optional: Makes the thread stop when the JVM exits
        mainThread.start();
    }

    public void stopTicker() {
        if (mainThread != null) {
            mainThread.interrupt();
            mainThread = null;
        }
    }
}
