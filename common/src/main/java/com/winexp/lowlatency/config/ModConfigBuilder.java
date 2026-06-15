package com.winexp.lowlatency.config;

import com.winexp.lowlatency.LowLatencyMod;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.network.chat.Component;

public class ModConfigBuilder implements ConfigEntryPoint {
    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        builder.registerOwnModOptions()
                .setIcon(LowLatencyMod.asResource("icon.png"))
                .addPage(builder.createOptionPage()
                        .setName(Component.translatable("low_latency.config.pages.general"))
                        .addOption(builder.createBooleanOption(LowLatencyMod.asResource("enabled"))
                                .setName(Component.translatable("low_latency.config.options.enabled"))
                                .setTooltip(Component.translatable("low_latency.config.options.enabled.tooltip"))
                                .setStorageHandler(ModConfig::save)
                                .setBinding(config -> ModConfig.INSTANCE.enabled = config, () ->  ModConfig.INSTANCE.enabled)
                                .setDefaultValue(true))
                        .addOption(builder.createIntegerOption(LowLatencyMod.asResource("wait_time_bias"))
                                .setName(Component.translatable("low_latency.config.options.wait_time_bias"))
                                .setTooltip(Component.translatable("low_latency.config.options.wait_time_bias.tooltip"))
                                .setStorageHandler(ModConfig::save)
                                .setBinding(config -> ModConfig.INSTANCE.wait_time_offset = config / 100.0, () -> (int) (ModConfig.INSTANCE.wait_time_offset * 100))
                                .setDefaultValue(0)
                                .setValueFormatter(value -> Component.literal(String.valueOf(value / 100.0)))
                                .setRange(-100, 100, 1)));
    }
}
