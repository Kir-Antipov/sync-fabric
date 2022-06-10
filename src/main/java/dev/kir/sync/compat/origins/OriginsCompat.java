package dev.kir.sync.compat.origins;

import dev.kir.sync.Sync;
import dev.kir.sync.api.shell.ShellStateComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

public class OriginsCompat implements EntityComponentInitializer {
    @Override
    public void registerEntityComponentFactories(@NotNull EntityComponentFactoryRegistry registry) {
        if (FabricLoader.getInstance().isModLoaded("origins") && !Sync.getConfig().preserveOrigins()) {
            ShellStateComponentFactoryRegistry.getInstance().register(OriginsShellStateComponent::new, OriginsShellStateComponent::new);
        }
    }
}
