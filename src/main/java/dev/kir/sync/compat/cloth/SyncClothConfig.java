package dev.kir.sync.compat.cloth;

import dev.kir.sync.Sync;
import dev.kir.sync.api.shell.ShellPriority;
import dev.kir.sync.config.SyncConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.entity.EntityType;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Config(name = Sync.MOD_ID)
public class SyncClothConfig implements SyncConfig, ConfigData {
    @ConfigEntry.Gui.Excluded
    @ConfigEntry.Category(value = "shell_construction") // -_-
    private static final SyncClothConfig INSTANCE = AutoConfig.register(SyncClothConfig.class, GsonConfigSerializer::new).getConfig();

    public static SyncConfig getInstance() {
        return INSTANCE;
    }

    @ConfigEntry.Category(value = "shell_construction")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean enableInstantShellConstruction = SyncConfig.super.enableInstantShellConstruction();

    @ConfigEntry.Category(value = "shell_construction")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean warnPlayerInsteadOfKilling = SyncConfig.super.warnPlayerInsteadOfKilling();

    @ConfigEntry.Category(value = "shell_construction")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public float fingerstickDamage = SyncConfig.super.fingerstickDamage();

    @ConfigEntry.Category(value = "shell_construction")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public float hardcoreFingerstickDamage = SyncConfig.super.hardcoreFingerstickDamage();

    @ConfigEntry.Category(value = "energy")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public long shellConstructorCapacity = SyncConfig.super.shellConstructorCapacity();

    @ConfigEntry.Category(value = "energy")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public long shellStorageCapacity = SyncConfig.super.shellStorageCapacity();

    @ConfigEntry.Category(value = "energy")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public long shellStorageConsumption = SyncConfig.super.shellStorageConsumption();

    @ConfigEntry.Category(value = "energy")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean shellStorageAcceptsRedstone = SyncConfig.super.shellStorageAcceptsRedstone();

    @ConfigEntry.Category(value = "energy")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public int shellStorageMaxUnpoweredLifespan = SyncConfig.super.shellStorageMaxUnpoweredLifespan();

    @ConfigEntry.Category(value = "energy")
    @ConfigEntry.Gui.Tooltip(count = 2)
    @ConfigEntry.Gui.RequiresRestart
    public List<EnergyMapEntry> energyMap = SyncConfig.super.energyMap().stream().map(x -> new EnergyMapEntry(x.entityId(), x.outputEnergyQuantity())).collect(Collectors.toCollection(ArrayList::new));

    @ConfigEntry.Category(value = "sync")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public List<ShellPriorityWrapper> syncPriority = SyncConfig.super.syncPriority().stream().map(x -> new ShellPriorityWrapper(x.priority())).collect(Collectors.toCollection(ArrayList::new));

    @ConfigEntry.Category(value = "misc")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public String wrench = SyncConfig.super.wrench();

    @ConfigEntry.Category(value = "misc")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean updateTranslationsAutomatically = SyncConfig.super.updateTranslationsAutomatically();


    @Override
    public boolean enableInstantShellConstruction() {
        return this.enableInstantShellConstruction;
    }

    @Override
    public boolean warnPlayerInsteadOfKilling() {
        return this.warnPlayerInsteadOfKilling;
    }

    @Override
    public float fingerstickDamage() {
        return this.fingerstickDamage;
    }

    @Override
    public float hardcoreFingerstickDamage() {
        return this.hardcoreFingerstickDamage;
    }

    @Override
    public long shellConstructorCapacity() {
        return this.shellConstructorCapacity;
    }

    @Override
    public long shellStorageCapacity() {
        return this.shellStorageCapacity;
    }

    @Override
    public long shellStorageConsumption() {
        return this.shellStorageConsumption;
    }

    @Override
    public boolean shellStorageAcceptsRedstone() {
        return this.shellStorageAcceptsRedstone;
    }

    @Override
    public int shellStorageMaxUnpoweredLifespan() {
        return this.shellStorageMaxUnpoweredLifespan;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SyncConfig.EnergyMapEntry> energyMap() {
        return (List<SyncConfig.EnergyMapEntry>)(List<?>)this.energyMap;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ShellPriorityEntry> syncPriority() {
        return (List<ShellPriorityEntry>)(List<?>)this.syncPriority;
    }

    @Override
    public String wrench() {
        return this.wrench;
    }

    @Override
    public boolean updateTranslationsAutomatically() {
        return this.updateTranslationsAutomatically;
    }


    public static class EnergyMapEntry implements SyncConfig.EnergyMapEntry {
        @ConfigEntry.Gui.RequiresRestart
        public String entityId;

        @ConfigEntry.Gui.RequiresRestart
        public long outputEnergyQuantity;

        public EnergyMapEntry() {
            this(EntityType.PIG, 16);
        }

        public EnergyMapEntry(EntityType<?> entityType, long outputEnergyQuantity) {
            this(Registry.ENTITY_TYPE.getId(entityType).toString(), outputEnergyQuantity);
        }

        public EnergyMapEntry(String entityId, long outputEnergyQuantity) {
            this.entityId = entityId;
            this.outputEnergyQuantity = outputEnergyQuantity;
        }

        @Override
        public String entityId() {
            return this.entityId;
        }

        @Override
        public long outputEnergyQuantity() {
            return this.outputEnergyQuantity;
        }
    }

    public static class ShellPriorityWrapper implements ShellPriorityEntry {
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ShellPriority priority;

        public ShellPriorityWrapper() {
            this(ShellPriority.NATURAL);
        }

        public ShellPriorityWrapper(ShellPriority priority) {
            this.priority = priority;
        }

        @Override
        public ShellPriority priority() {
            return this.priority;
        }
    }
}
