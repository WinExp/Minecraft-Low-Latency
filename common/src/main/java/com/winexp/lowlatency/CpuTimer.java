package com.winexp.lowlatency;

public class CpuTimer {
    private long startTime = System.nanoTime();
    private long endTime = System.nanoTime();

    public void start() {
        startTime = System.nanoTime();
    }

    public void stop() {
        endTime = System.nanoTime();
    }

    public long getTimeElapsed() {
        return endTime - startTime;
    }
}
