package dev.kir.sync.config;

import dev.kir.sync.Sync;
import dev.kir.sync.api.shell.ShellPriority;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

@Config(name = Sync.MOD_ID)
public class SyncConfig implements ConfigData {
    @ConfigEntry.Category(value = "shell_construction")
    public boolean enableInstantShellConstruction = false;

    @ConfigEntry.Category(value = "shell_construction")
    public boolean warnPlayerInsteadOfKilling = false;

    @ConfigEntry.Category(value = "shell_construction")
    public float fingerstickDamage = 20;

    @ConfigEntry.Category(value = "shell_construction")
    public float hardcoreFingerstickDamage = 40;

    @ConfigEntry.Category(value = "sync")
    public List<ShellPriorityWrapper> syncPriority = new ArrayList<>(List.of(new ShellPriorityWrapper()));

    @ConfigEntry.Category(value = "misc")
    public boolean updateTranslationsAutomatically = false;

    public static class ShellPriorityWrapper {
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ShellPriority priority;

        public ShellPriorityWrapper() {
            this(ShellPriority.NATURAL);
        }

        public ShellPriorityWrapper(ShellPriority priority) {
            this.priority = priority;
        }
    }
}
