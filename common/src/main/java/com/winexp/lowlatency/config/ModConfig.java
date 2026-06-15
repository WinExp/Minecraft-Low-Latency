package com.winexp.lowlatency.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.winexp.lowlatency.LowLatencyMod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    public static final ModConfig INSTANCE;
    public static final Path CONFIG_FILE_PATH = Path.of("config/low-latency.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean enabled = true;
    public int wait_time_bias_ms = 0;

    static {
        ModConfig config;
        try {
            String configJson = Files.readString(CONFIG_FILE_PATH);
            config = GSON.fromJson(configJson, ModConfig.class);
        } catch (IOException e) {
            config = new ModConfig();
        }
        INSTANCE = config;
    }

    public static void save() {
        String configJson = GSON.toJson(INSTANCE);
        try {
            Files.writeString(CONFIG_FILE_PATH, configJson);
        } catch (IOException e) {
            LowLatencyMod.LOGGER.error("Failed to save configuration", e);
        }
    }
}
