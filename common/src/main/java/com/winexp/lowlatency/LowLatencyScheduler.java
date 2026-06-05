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
    private long lastSubmitTime = System.nanoTime();
    private long estimatedLatency;

    public long getAverageCpuTime() {
        return cpuTimeTracker.getAverageFrameTime();
    }

    public long getAverageGpuTime() {
        return gpuTimeTracker.getAverageFrameTime();
    }

    public int getQueueLength() {
        return gpuTimerQueue.size();
    }

    public long getEstimatedLatency() {
        return estimatedLatency;
    }

    public static void wait(LowLatencyScheduler scheduler) {
        if (!LowLatencyMod.CONFIG.enabled) return;
        long waitTime = scheduler.getEstimatedLatency()
                + (long) (LowLatencyMod.CONFIG.wait_time_offset * scheduler.getAverageGpuTime());
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
        cpuTimeTracker.addFrame(cpuTimer.getTimeElapsedNs());
    }

    public void recordGpuBegin() {
        currentGpuTimer = gpuTimerPool.borrowObject();
        currentGpuTimer.recordBegin();
    }

    public void recordGpuEnd() {
        currentGpuTimer.recordEnd();
        gpuTimerQueue.add(currentGpuTimer);
        currentGpuTimer = null;
        lastSubmitTime = System.nanoTime();
    }

    public void checkGpu() {
        Iterator<GpuTimer> it = gpuTimerQueue.iterator();
        while (it.hasNext()) {
            GpuTimer gpuTimer = it.next();
            gpuTimer.updateResult();
            if (gpuTimer.getState() == GpuTimer.State.RESULT_AVAILABLE) {
                long gpuElapsed = gpuTimer.getTimeElapsedNs();
                it.remove();
                gpuTimerPool.returnObject(gpuTimer);
                gpuTimeTracker.addFrame(gpuElapsed);
            } else break;
        }
        estimatedLatency = getAverageGpuTime() * getQueueLength()
                - getAverageCpuTime()
                - (System.nanoTime() - lastSubmitTime);
        estimatedLatency = estimatedLatency > 0 ? estimatedLatency : 0;
    }

    @Override
    public void close() {
        for (GpuTimer gpuTimer : gpuTimerQueue) {
            gpuTimer.close();
        }
        gpuTimerPool.close();
    }
}
