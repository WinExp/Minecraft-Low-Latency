package com.winexp.lowlatency;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;

public class FrameTimeTracker {
    private final LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
    private final int window;
    private long sum;

    public FrameTimeTracker(int window) {
        this.window = window;
    }

    public long getAverageFrameTime() {
        if (queue.isEmpty()) return 0;
        return sum / queue.size();
    }

    public void addFrame(long frameTime) {
        queue.enqueue(frameTime);
        sum += frameTime;
        if (queue.size() > window) {
            sum -= queue.dequeueLong();
        }
    }
}
