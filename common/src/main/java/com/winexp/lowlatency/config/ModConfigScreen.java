package com.winexp.lowlatency.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModConfigScreen {
    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("low_latency.config.title"))
                .setSavingRunnable(ModConfig::save);

        ConfigCategory category = builder.getOrCreateCategory(Component.translatable("low_latency.config.categories.general"));
        category.addEntry(
                builder.entryBuilder()
                        .startBooleanToggle(Component.translatable("low_latency.config.options.enabled"), ModConfig.INSTANCE.enabled)
                        .setDefaultValue(true)
                        .setSaveConsumer(config -> ModConfig.INSTANCE.enabled = config)
                        .build()
        );
        category.addEntry(
                builder.entryBuilder()
                        .startIntField(Component.translatable("low_latency.config.options.wait_time_bias"), ModConfig.INSTANCE.wait_time_bias_ms)
                        .setDefaultValue(0)
                        .setSaveConsumer(config -> ModConfig.INSTANCE.wait_time_bias_ms = config)
                        .build()
        );
        return builder.build();
    }
}
