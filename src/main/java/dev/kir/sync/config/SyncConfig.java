package dev.kir.sync.config;

import dev.kir.sync.Sync;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = Sync.MOD_ID)
public class SyncConfig implements ConfigData {
    @ConfigEntry.Category(value = "misc")
    public boolean updateTranslationsAutomatically = false;

}
