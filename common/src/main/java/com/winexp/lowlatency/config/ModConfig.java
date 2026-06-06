package com.winexp.lowlatency.config;

import com.winexp.lowlatency.LowLatencyMod;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = LowLatencyMod.MOD_ID)
public class ModConfig implements ConfigData {
    public boolean enabled = true;
    public double wait_time_offset = 0;

    public void validatePostLoad() throws ValidationException {
        if (wait_time_offset < -100 || wait_time_offset > 100)
            throw new ValidationException("Wait time offset must be between -100 and 100");
    }
}
