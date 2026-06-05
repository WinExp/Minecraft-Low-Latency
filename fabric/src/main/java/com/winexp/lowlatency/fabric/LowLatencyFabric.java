package com.winexp.lowlatency.fabric;

import com.winexp.lowlatency.LowLatencyMod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;

public class LowLatencyFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        LowLatencyMod.init();
        LowLatencyMod.registerDebugScreenEntries(DebugScreenEntries::register);
        ClientLifecycleEvents.CLIENT_STOPPING.register(LowLatencyMod::onClientStopping);
    }
}
