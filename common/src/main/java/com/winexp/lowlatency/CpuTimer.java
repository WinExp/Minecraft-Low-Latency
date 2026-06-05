package com.winexp.lowlatency;

public class CpuTimer {
    private long lastTime = System.nanoTime();
    private long result;

    public void startRecord() {
        lastTime = System.nanoTime();
    }

    public void endRecord() {
        result = System.nanoTime() - lastTime;
    }

    public long getTimeElapsedNs() {
        return result;
    }
}
