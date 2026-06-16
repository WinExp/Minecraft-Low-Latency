package com.winexp.lowlatency.fabric;

import com.winexp.lowlatency.LowLatencyMod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class LowLatencyFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        LowLatencyMod.init();
        ClientLifecycleEvents.CLIENT_STOPPING.register(LowLatencyMod::onClientStopping);
    }
}
