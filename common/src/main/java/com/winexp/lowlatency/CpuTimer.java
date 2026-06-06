package com.winexp.lowlatency;

public class CpuTimer {
    private long startTime = System.nanoTime();
    private long endTime = System.nanoTime();

    public void startRecord() {
        startTime = System.nanoTime();
    }

    public void endRecord() {
        endTime = System.nanoTime();
    }

    public long getTimeElapsed() {
        return endTime - startTime;
    }
}
