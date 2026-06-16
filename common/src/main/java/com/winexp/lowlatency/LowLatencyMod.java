package com.winexp.lowlatency;

import com.winexp.lowlatency.gui.debug.DebugEntryStatistics;
import com.winexp.lowlatency.mixin.DebugScreenEntriesAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LowLatencyMod {
    public static final String MOD_ID = "low_latency";
    public static final String MOD_NAME = "Minecraft Low Latency";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final LowLatencyScheduler SCHEDULER = new LowLatencyScheduler();
    public static final Identifier STATISTICS_DEBUG_ENTRY = asResource("statistics");

    public static void init() {
        DebugScreenEntriesAccessor.register(STATISTICS_DEBUG_ENTRY, new DebugEntryStatistics());
    }

    public static void onClientStopping(Minecraft client) {
        SCHEDULER.close();
    }

    public static Identifier asResource(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}