package com.winexp.lowlatency;

import com.winexp.lowlatency.config.ModConfig;
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

    public boolean isEnabled() {
        return ModConfig.INSTANCE.enabled;
    }

    public void beforePoll() {
        pollGpuState();
        long waitTime = getAverageGpuLatency()
                - getAverageCpuTime();
        waitTime = (long) (waitTime * (1 + ModConfig.INSTANCE.wait_time_offset));
        waitTime = isEnabled() && waitTime > 0 ? waitTime : 0;
        statistics.waitTime = waitTime;
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

    public void afterPoll() {
        cpuTimer.startRecord();
    }

    public void beforeRender() {
        GpuTimer gpuTimer = gpuTimerPool.borrowObject();
        gpuTimer.recordBegin();
        gpuTimerQueue.add(gpuTimer);
    }

    public void beforeClip() {
        cpuTimer.endRecord();
        cpuTimeTracker.addFrame(cpuTimer.getTimeElapsed());

        GpuTimer currentFrameGpuTimer = gpuTimerQueue.getLast();
        pollGpuState();
        statistics.frameQueueBacklog = gpuTimerQueue.size();
        if (gpuTimerQueue.contains(currentFrameGpuTimer)) statistics.frameQueueBacklog--;
    }

    public void afterClip() {
        GpuTimer gpuTimer = gpuTimerQueue.getLast();
        gpuTimer.recordEnd();
    }

    private void pollGpuState() {
        Iterator<GpuTimer> it = gpuTimerQueue.iterator();
        while (it.hasNext()) {
            GpuTimer gpuTimer = it.next();
            gpuTimer.pollResult();
            if (gpuTimer.getState() == GpuTimer.State.RESULT_AVAILABLE) {
                gpuLatencyTracker.addFrame(gpuTimer.getCompletionDelay());
                statistics.gpuTimeTracker.addFrame(gpuTimer.getTimeElapsed());
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
            return LowLatencyScheduler.this.getAverageCpuTime();
        }

        public long getAverageGpuTime() {
            return gpuTimeTracker.getAverageTime();
        }

        public long getAverageGpuLatency() {
            return LowLatencyScheduler.this.getAverageGpuLatency();
        }

        public long getWaitTime() {
            return waitTime;
        }

        public long getFrameQueueBacklog() {
            return frameQueueBacklog;
        }
    }
}
