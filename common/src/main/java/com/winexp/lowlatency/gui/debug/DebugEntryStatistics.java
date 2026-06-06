package com.winexp.lowlatency.gui.debug;

import com.winexp.lowlatency.LowLatencyMod;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class DebugEntryStatistics implements DebugScreenEntry {
    private static final Identifier MAIN = LowLatencyMod.asResource("main");

    @Override
    public void display(DebugScreenDisplayer displayer, Level level, LevelChunk clientChunk, LevelChunk serverChunk) {
        String status = LowLatencyMod.CONFIG.enabled ? "enabled" : "disabled";
        displayer.addToGroup(MAIN, String.format("Low latency mode: %s", status));
        displayer.addToGroup(MAIN, String.format("Average CPU time: %.4f ms", LowLatencyMod.SCHEDULER.statistics.getAverageCpuTime() / 1_000_000.0));
        displayer.addToGroup(MAIN, String.format("Average GPU time: %.4f ms", LowLatencyMod.SCHEDULER.statistics.getAverageGpuTime() / 1_000_000.0));
        displayer.addToGroup(MAIN, String.format("Average GPU completion delay: %.4f ms", LowLatencyMod.SCHEDULER.statistics.getAverageGpuCompletionDelay() / 1_000_000.0));
        displayer.addToGroup(MAIN, String.format("Wait time: %.4f ms", LowLatencyMod.SCHEDULER.statistics.getWaitTime() / 1_000_000.0));
        displayer.addToGroup(MAIN, String.format("Frame queue backlog: %d", LowLatencyMod.SCHEDULER.statistics.getFrameQueueBacklog()));
    }
}
