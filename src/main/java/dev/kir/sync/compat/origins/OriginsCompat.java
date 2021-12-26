package dev.kir.sync.compat.origins;

import dev.kir.sync.api.shell.ShellStateComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class OriginsCompat implements EntityComponentInitializer {
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        if (FabricLoader.getInstance().isModLoaded("origins")) {
            ShellStateComponentFactoryRegistry.getInstance().register(OriginsShellStateComponent::new, OriginsShellStateComponent::new);
        }
    }
}
