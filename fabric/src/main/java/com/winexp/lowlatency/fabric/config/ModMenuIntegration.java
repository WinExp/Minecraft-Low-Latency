package com.winexp.lowlatency.fabric.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.util.NullScreenFactory;
import com.winexp.lowlatency.config.ModConfigScreen;
import com.winexp.lowlatency.platform.Services;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (Services.PLATFORM.isModLoaded("cloth-config"))  {
            return ModConfigScreen::create;
        } else {
            return new NullScreenFactory<>();
        }
    }
}
