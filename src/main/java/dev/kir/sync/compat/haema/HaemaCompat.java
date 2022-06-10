package dev.kir.sync.compat.haema;

import dev.kir.sync.api.shell.ShellStateComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

public class HaemaCompat implements EntityComponentInitializer {
    @Override
    public void registerEntityComponentFactories(@NotNull EntityComponentFactoryRegistry registry) {
        if (FabricLoader.getInstance().isModLoaded("haema")) {
            ShellStateComponentFactoryRegistry.getInstance().register(HaemaShellStateComponent::new);
        }
    }
}
