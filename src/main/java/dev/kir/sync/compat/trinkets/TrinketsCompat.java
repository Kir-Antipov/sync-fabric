package dev.kir.sync.compat.trinkets;

import dev.emi.trinkets.api.TrinketsApi;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.kir.sync.api.shell.ShellStateComponentFactoryRegistry;
import dev.kir.sync.entity.ShellEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityType;

public class TrinketsCompat implements EntityComponentInitializer {
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        if (!FabricLoader.getInstance().isModLoaded("trinkets")) {
            return;
        }

        ShellStateComponentFactoryRegistry.getInstance().register(() -> new TrinketShellStateComponent(EntityType.PLAYER), TrinketShellStateComponent::new);
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            registry.registerFor(ShellEntity.class, TrinketsApi.TRINKET_COMPONENT, ShellEntityTrinketComponent::new);
        }
    }
}