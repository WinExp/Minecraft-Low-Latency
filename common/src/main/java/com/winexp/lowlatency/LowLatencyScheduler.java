package com.winexp.lowlatency;

import com.winexp.lowlatency.config.ModConfig;

import java.util.concurrent.locks.LockSupport;

public class LowLatencyScheduler implements AutoCloseable {
    private static final int FRAME_SAMPLING_WINDOW = 6;
    private static final long WAIT_SPIN_THRESHOLD = 500_000;

    public final CpuTimer cpuTimer = new CpuTimer();
    private final FrameTimer frameTimer;
    private final FrameTimeTracker cpuTimeTracker = new FrameTimeTracker(FRAME_SAMPLING_WINDOW);
    private final FrameTimeTracker gpuTimeTracker = new FrameTimeTracker(FRAME_SAMPLING_WINDOW);
    public final Statistics statistics = new Statistics();

    public LowLatencyScheduler() {
        frameTimer = new FrameTimer(frame -> gpuTimeTracker.addFrame(frame.getGpuTime()));
    }

    private long getAverageCpuTime() {
        return cpuTimeTracker.getAverageTime();
    }

    private long getAverageGpuTime() {
        return gpuTimeTracker.getAverageTime();
    }

    private long getEstimatedGpuLatency() {
        long estimated =
                (long) (getAverageGpuTime() * (frameTimer.getAwaitingCount() + ModConfig.INSTANCE.wait_time_offset))
                - getAverageCpuTime();
        return estimated <= 0 ? 0 : estimated;
    }

    public boolean isEnabled() {
        return ModConfig.INSTANCE.enabled;
    }

    public void beforePoll() {
        frameTimer.poll();
        long waitTime = getEstimatedGpuLatency();
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
        cpuTimer.start();
    }

    public void beforeRender() {
        frameTimer.beginProfile();
    }

    public void beforeSubmit() {
        cpuTimer.stop();
        cpuTimeTracker.addFrame(cpuTimer.getTimeElapsed());

        frameTimer.poll();
        statistics.frameQueueBacklog = frameTimer.getAwaitingCount();

        frameTimer.endProfile(cpuTimer);
    }

    public void afterSubmit() {
        frameTimer.submit();
    }

    @Override
    public void close() {
        frameTimer.close();
    }

    public class Statistics {
        private long waitTime;
        private long frameQueueBacklog;

        public long getAverageCpuTime() {
            return LowLatencyScheduler.this.getAverageCpuTime();
        }

        public long getAverageGpuTime() {
            return LowLatencyScheduler.this.getAverageGpuTime();
        }

        public long getEstimatedGpuLatency() {
            return LowLatencyScheduler.this.getEstimatedGpuLatency();
        }

        public long getWaitTime() {
            return waitTime;
        }

        public long getFrameQueueBacklog() {
            return frameQueueBacklog;
        }
    }
}
