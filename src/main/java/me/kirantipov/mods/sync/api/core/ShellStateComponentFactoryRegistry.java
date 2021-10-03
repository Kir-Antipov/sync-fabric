package me.kirantipov.mods.sync.api.core;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ShellStateComponentFactoryRegistry {
    static ShellStateComponentFactoryRegistry getInstance() {
        return ShellStateComponentFactoryRegistryImpl.INSTANCE;
    }

    ShellStateComponentFactory register(ShellStateComponentFactory factory);

    Set<ShellStateComponentFactory> getValues();

    default ShellStateComponentFactory register(Supplier<ShellStateComponent> emptyFactory, Function<ServerPlayerEntity, ShellStateComponent> factory) {
        return register(new ShellStateComponentFactory() {
            @Override
            public ShellStateComponent empty() {
                return emptyFactory.get();
            }

            @Override
            public ShellStateComponent of(ServerPlayerEntity player) {
                return factory.apply(player);
            }
        });
    }

    default ShellStateComponent createEmpty() {
        Set<ShellStateComponentFactory> factories = this.getValues();
        List<ShellStateComponent> components = new ArrayList<>(factories.size());
        for (ShellStateComponentFactory factory : factories) {
            components.add(factory.empty());
        }
        return ShellStateComponent.combine(components);
    }

    default ShellStateComponent createOf(ServerPlayerEntity player) {
        Set<ShellStateComponentFactory> factories = this.getValues();
        List<ShellStateComponent> components = new ArrayList<>(factories.size());
        for (ShellStateComponentFactory factory : factories) {
            components.add(factory.of(player));
        }
        return ShellStateComponent.combine(components);
    }

    interface ShellStateComponentFactory {
        ShellStateComponent empty();

        ShellStateComponent of(ServerPlayerEntity player);
    }
}
