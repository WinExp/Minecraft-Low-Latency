package com.winexp.lowlatency.neoforge;

import com.winexp.lowlatency.LowLatencyMod;
import com.winexp.lowlatency.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfigClient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(LowLatencyMod.MOD_ID)
public class LowLatencyNeoForge {
    public LowLatencyNeoForge(IEventBus eventBus, ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (_, parent) -> AutoConfigClient.getConfigScreen(ModConfig.class, parent).get());
        LowLatencyMod.init();
        eventBus.addListener(RegisterDebugEntriesEvent.class, event ->
                LowLatencyMod.registerDebugScreenEntries(event::register));
        NeoForge.EVENT_BUS.addListener(ClientStoppingEvent.class, event ->
                LowLatencyMod.onClientStopping(event.getClient()));
    }
}