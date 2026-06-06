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
    private final FrameTimeTracker cpuTimeTracker = new FrameTimeTracker(FRAME_SAMPLING_WINDOW);
    private final FrameTimeTracker gpuLatencyTracker = new FrameTimeTracker(FRAME_SAMPLING_WINDOW);
    public final Statistics statistics = new Statistics();

    private long getAverageCpuTime() {
        return cpuTimeTracker.getAverageTime();
    }

    private long getAverageGpuLatency() {
        return gpuLatencyTracker.getAverageTime();
    }

    public static void wait(LowLatencyScheduler scheduler) {
        if (!LowLatencyMod.CONFIG.enabled) return;

        LowLatencyMod.SCHEDULER.updateGpuStatus();
        long waitTime = scheduler.getAverageGpuLatency()
                - scheduler.getAverageCpuTime()
                + (long) (LowLatencyMod.CONFIG.wait_time_offset * 1_000_000);
        waitTime = waitTime > 0 ? waitTime : 0;
        scheduler.statistics.waitTime = waitTime;
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
        GpuTimer gpuTimer = gpuTimerPool.borrowObject();
        gpuTimer.recordBegin();
        gpuTimerQueue.add(gpuTimer);
    }

    public void recordGpuEnd() {
        GpuTimer gpuTimer = gpuTimerQueue.getLast();
        gpuTimer.recordEnd();
    }

    private void updateGpuStatus() {
        Iterator<GpuTimer> it = gpuTimerQueue.iterator();
        while (it.hasNext()) {
            GpuTimer gpuTimer = it.next();
            gpuTimer.updateResult();
            if (gpuTimer.getState() == GpuTimer.State.RESULT_AVAILABLE) {
                statistics.gpuTimeTracker.addFrame(gpuTimer.getTimeElapsed());
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

    public class Statistics {
        private final FrameTimeTracker gpuTimeTracker = new FrameTimeTracker(FRAME_SAMPLING_WINDOW);

        private long waitTime;
        private long frameQueueBacklog;

        public long getAverageCpuTime() {
            return cpuTimeTracker.getAverageTime();
        }

        public long getAverageGpuTime() {
            return gpuTimeTracker.getAverageTime();
        }

        public long getAverageGpuLatency() {
            return gpuLatencyTracker.getAverageTime();
        }

        public long getWaitTime() {
            return waitTime;
        }

        public long getFrameQueueBacklog() {
            return frameQueueBacklog;
        }

        public void updateFrameQueueBacklog(){
            GpuTimer lastGpuTimer = gpuTimerQueue.getLast();
            updateGpuStatus();
            frameQueueBacklog = gpuTimerQueue.size();
            if (gpuTimerQueue.contains(lastGpuTimer)) frameQueueBacklog--;
        }
    }
}
