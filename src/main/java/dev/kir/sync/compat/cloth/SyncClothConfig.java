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
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
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
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.RequiresRestart
    public boolean preserveOrigins = SyncConfig.super.preserveOrigins();

    @ConfigEntry.Category(value = "sync")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public List<ShellPriorityWrapper> syncPriority = SyncConfig.super.syncPriority().stream().map(x -> new ShellPriorityWrapper(x.priority())).collect(Collectors.toCollection(ArrayList::new));

    @ConfigEntry.Category(value = "misc")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public String wrench = SyncConfig.super.wrench();

    @ConfigEntry.Category(value = "misc")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean updateTranslationsAutomatically = SyncConfig.super.updateTranslationsAutomatically();

    @ConfigEntry.Category(value = "easter_eggs")
    @ConfigEntry.Gui.TransitiveObject
    public EasterEggs easterEggs = new EasterEggs();

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

    @Override
    public boolean preserveOrigins() {
        return this.preserveOrigins;
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

    @Override
    public boolean enableTechnobladeEasterEgg() {
        return this.easterEggs.technoblade.enable;
    }

    @Override
    public boolean renderTechnobladeCape() {
        return this.easterEggs.technoblade.renderCape;
    }

    @Override
    public boolean allowTechnobladeAnnouncements() {
        return this.easterEggs.technoblade.allowAnnouncements;
    }

    @Override
    public boolean allowTechnobladeQuotes() {
        return this.easterEggs.technoblade.allowQuotes;
    }

    @Override
    public int TechnobladeQuoteDelay() {
        return this.easterEggs.technoblade.quoteDelay;
    }

    @Override
    public boolean isTechnoblade(UUID uuid) {
        return this.easterEggs.technoblade.cache.contains(uuid);
    }

    @Override
    public void addTechnoblade(UUID uuid) {
        this.easterEggs.technoblade.cache.add(uuid);
        AutoConfig.getConfigHolder(SyncClothConfig.class).save();
    }

    @Override
    public void removeTechnoblade(UUID uuid) {
        this.easterEggs.technoblade.cache.remove(uuid);
        AutoConfig.getConfigHolder(SyncClothConfig.class).save();
    }

    @Override
    public void clearTechnobladeCache() {
        this.easterEggs.technoblade.cache.clear();
        AutoConfig.getConfigHolder(SyncClothConfig.class).save();
    }

    public static class EasterEggs {
        @ConfigEntry.Gui.CollapsibleObject
        public TechnobladeEasterEgg technoblade = new TechnobladeEasterEgg();
    }

    public static class TechnobladeEasterEgg {
        @ConfigEntry.Gui.RequiresRestart
        public boolean enable = true;
        public boolean renderCape = false;
        public boolean allowAnnouncements = true;
        public boolean allowQuotes = true;
        public int quoteDelay = 1800;

        @ConfigEntry.Gui.Excluded
        public HashSet<UUID> cache = new HashSet<>();
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
