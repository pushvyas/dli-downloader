package org.shunya.dli;

public interface InteractiveTask {
    String getName();
    int getProgress();
    void pause();
    void resume();
    void remove();
    void stop();
    void stopAsync();
    void awaitShutdown();
    RunState getState();
    String getTimeLeft();
    void addObserver(DownloadObserver observer);
}
