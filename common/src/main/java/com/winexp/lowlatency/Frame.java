package com.winexp.lowlatency;

import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.systems.GpuQueryPool;

import java.util.OptionalLong;

public class Frame {
    private final GpuFence fence;
    private final CpuTimer cpuTimer;
    private final GpuQueryPool queryPool;
    private final int queryIdx;
    private Long gpuTime;

    public Frame(GpuFence fence, CpuTimer cpuTimer, GpuQueryPool queryPool, int queryIdx) {
        this.fence = fence;
        this.cpuTimer = cpuTimer;
        this.queryPool = queryPool;
        this.queryIdx = queryIdx;
    }

    public OptionalLong[] getQueryValues(int count) {
        return queryPool.getValues(queryIdx, count);
    }

    public GpuFence getFence() {
        return fence;
    }

    public CpuTimer getCpuTimer() {
        return cpuTimer;
    }

    public long getGpuTime() {
        if (gpuTime == null) throw new IllegalStateException("gpuTime is empty");
        return gpuTime;
    }

    public void setGpuTime(long gpuTime) {
        if (this.gpuTime != null) throw new IllegalStateException("gpuTime is not empty");
        this.gpuTime = gpuTime;
    }

    public boolean isCompleted() {
        return gpuTime != null;
    }
}
