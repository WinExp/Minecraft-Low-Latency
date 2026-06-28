package com.winexp.lowlatency;

import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuQueryPool;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.OptionalLong;
import java.util.Queue;
import java.util.function.Consumer;

public class FrameTimer implements AutoCloseable {
    private static final int ROTATIONS = 256;
    private final GpuQueryPool queryPool = RenderSystem.getDevice().createTimestampQueryPool(ROTATIONS * 2);
    private int currentRotationIdx;
    @Nullable
    private CommandEncoder activeEncoder;
    private Status status = Status.NOT_RECORDING;
    private Frame currentFrame;
    private final Queue<Frame> awaitingFrames = new ArrayDeque<>();
    private final Consumer<Frame> frameCallback;

    public FrameTimer(Consumer<Frame> frameCallback) {
        this.frameCallback = frameCallback;
    }

    public int getAwaitingCount() {
        return awaitingFrames.size();
    }

    public void poll() {
        while (!awaitingFrames.isEmpty()) {
            Frame frame = awaitingFrames.element();
            GpuFence fence = frame.getFence();
            if (!fence.awaitCompletion(0)) {
                break;
            }
            OptionalLong[] queryValues = frame.getQueryValues(2);
            OptionalLong startValue = queryValues[0];
            OptionalLong endValue = queryValues[1];
            if (startValue.isEmpty() || endValue.isEmpty()) throw new IllegalStateException();
            long gpuTime = (long) ((endValue.getAsLong() - startValue.getAsLong()) * RenderSystem.getDevice().getDeviceInfo().timestampPeriod());
            frame.setGpuTime(gpuTime);
            awaitingFrames.remove();
            frameCallback.accept(frame);
        }
    }

    public void beginProfile() {
        if (status != Status.NOT_RECORDING || activeEncoder != null) throw new IllegalStateException("Timer is not ready");
        currentRotationIdx++;
        currentRotationIdx %= ROTATIONS;
        activeEncoder = RenderSystem.getDevice().createCommandEncoder();
        activeEncoder.writeTimestamp(queryPool, currentRotationIdx * 2);
        status = Status.STARTED;
    }

    public void endProfile(CpuTimer cpuTimer) {
        if (status != Status.STARTED || activeEncoder == null) throw new IllegalStateException("Timer is not recording");
        activeEncoder.writeTimestamp(queryPool, currentRotationIdx * 2 + 1);
        GpuFence fence = activeEncoder.createFence();
        activeEncoder = null;
        currentFrame = new Frame(fence, cpuTimer, queryPool, currentRotationIdx * 2);
        status = Status.NOT_RECORDING;
    }

    public void submit() {
        awaitingFrames.add(currentFrame);
        currentFrame = null;
    }

    @Override
    public void close() {
        queryPool.close();
    }

    public enum Status {
        NOT_RECORDING,
        STARTED
    }
}
