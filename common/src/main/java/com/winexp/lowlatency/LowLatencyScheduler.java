package com.winexp.lowlatency;

import com.winexp.lowlatency.util.ObjectPool;

import java.io.Closeable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.locks.LockSupport;

public class LowLatencyScheduler implements Closeable {
    private static final int FRAME_SAMPLING_WINDOW = 6;
    private static final long WAIT_SPIN_THRESHOLD = 500_000;

    private final CpuTimer cpuTimer = new CpuTimer();
    private final ObjectPool<GpuTimer> gpuTimerPool = new ObjectPool<>(
            GpuTimer::new,
            GpuTimer::reset
    );
    private final Deque<GpuTimer> gpuTimerQueue = new ArrayDeque<>();
    private GpuTimer currentGpuTimer = null;
    private final FrameTimeTracker cpuTimeTracker = new FrameTimeTracker(FRAME_SAMPLING_WINDOW);
    private final FrameTimeTracker gpuTimeTracker = new FrameTimeTracker(FRAME_SAMPLING_WINDOW);
    private final FrameTimeTracker gpuLatencyTracker = new FrameTimeTracker(FRAME_SAMPLING_WINDOW);

    public long getAverageCpuTime() {
        return cpuTimeTracker.getAverageFrameTime();
    }

    public long getAverageGpuTime() {
        return gpuTimeTracker.getAverageFrameTime();
    }

    public long getAverageGpuLatency() {
        return gpuLatencyTracker.getAverageFrameTime();
    }

    public int getQueueLength() {
        return gpuTimerQueue.size();
    }

    public long getWaitTime() {
        long waitTime = getAverageGpuLatency() - getAverageCpuTime();
        return waitTime > 0 ? waitTime : 0;
    }

    public static void wait(LowLatencyScheduler scheduler) {
        if (!LowLatencyMod.CONFIG.enabled) return;
        long waitTime = scheduler.getWaitTime();
        if (waitTime <= 0) return;
        long target = System.nanoTime() + waitTime;
        while (true) {
            long remaining = target - System.nanoTime();
            if (remaining <= 0) break;
            if (remaining > WAIT_SPIN_THRESHOLD) {
                if (!Thread.interrupted()) {
                    LockSupport.parkNanos(remaining - WAIT_SPIN_THRESHOLD);
                }
            } else {
                Thread.onSpinWait();
            }
        }
    }

    public void recordCpuBegin() {
        cpuTimer.startRecord();
    }

    public void recordCpuEnd() {
        cpuTimer.endRecord();
        cpuTimeTracker.addFrame(cpuTimer.getTimeElapsed());
    }

    public void recordGpuBegin() {
        currentGpuTimer = gpuTimerPool.borrowObject();
        currentGpuTimer.recordBegin();
    }

    public void recordGpuEnd() {
        currentGpuTimer.recordEnd();
        gpuTimerQueue.add(currentGpuTimer);
        currentGpuTimer = null;
    }

    public void checkGpu() {
        Iterator<GpuTimer> it = gpuTimerQueue.iterator();
        while (it.hasNext()) {
            GpuTimer gpuTimer = it.next();
            gpuTimer.updateResult();
            if (gpuTimer.getState() == GpuTimer.State.RESULT_AVAILABLE) {
                gpuTimeTracker.addFrame(gpuTimer.getTimeElapsed());
                gpuLatencyTracker.addFrame(gpuTimer.getLatency());
                it.remove();
                gpuTimerPool.returnObject(gpuTimer);
            } else break;
        }
    }

    @Override
    public void close() {
        for (GpuTimer gpuTimer : gpuTimerQueue) {
            gpuTimer.close();
        }
        gpuTimerPool.close();
    }
}
