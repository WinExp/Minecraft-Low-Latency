package com.winexp.lowlatency.gui.debug;

import com.winexp.lowlatency.LowLatencyMod;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class DebugEntryLatency implements DebugScreenEntry {
    private static final Identifier MAIN = LowLatencyMod.asResource("main");

    @Override
    public void display(DebugScreenDisplayer displayer, Level level, LevelChunk clientChunk, LevelChunk serverChunk) {
        String status = LowLatencyMod.CONFIG.enabled ? "enabled" : "disabled";
        displayer.addToGroup(MAIN, String.format("Low latency: %s", status));
        displayer.addToGroup(MAIN, String.format("Average CPU Time: %.4f ms", LowLatencyMod.SCHEDULER.getAverageCpuTime() / 1_000_000.0));
        displayer.addToGroup(MAIN, String.format("Average GPU Time: %.4f ms", LowLatencyMod.SCHEDULER.getAverageGpuTime() / 1_000_000.0));
        displayer.addToGroup(MAIN, String.format("Estimated Latency: %.4f ms", LowLatencyMod.SCHEDULER.getEstimatedLatency() / 1_000_000.0));
        displayer.addToGroup(MAIN, String.format("Queue length: %d", LowLatencyMod.SCHEDULER.getQueueLength()));
    }
}
