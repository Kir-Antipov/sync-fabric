package me.kirantipov.mods.sync.api.shell;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An object that allows registering custom {@link ShellStateComponent}.
 */
public interface ShellStateComponentFactoryRegistry {
    /**
     * @return The public-facing {@link ShellStateComponentFactoryRegistry} instance.
     */
    static ShellStateComponentFactoryRegistry getInstance() {
        return ShellStateComponentFactoryRegistryImpl.INSTANCE;
    }


    /**
     * Registers a new factory.
     * @param factory The factory.
     * @return The registered factory.
     */
    ShellStateComponentFactory register(ShellStateComponentFactory factory);

    /**
     * @return All the registered factories.
     */
    Collection<ShellStateComponentFactory> getValues();


    /**
     * Registers a new factory.
     * @param factory The factory.
     * @return The registered factory.
     */
    default ShellStateComponentFactory register(Function<@Nullable ServerPlayerEntity, ShellStateComponent> factory) {
        return register(new ShellStateComponentFactory() {
            @Override
            public ShellStateComponent empty() {
                return factory.apply(null);
            }

            @Override
            public ShellStateComponent of(ServerPlayerEntity player) {
                return factory.apply(player);
            }
        });
    }

    /**
     * Registers a new factory.
     * @param emptyFactory Creates a new instance of {@linkplain ShellStateComponent} that has no player data.
     * @param factory Creates a new instance of {@linkplain ShellStateComponent} that is synced with the player's state.
     * @return The registered factory.
     */
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


    /**
     * {@link ShellStateComponent}'s factory.
     */
    interface ShellStateComponentFactory {
        /**
         * Creates a new instance of {@link ShellStateComponent} that has no player data.
         * @return The {@linkplain ShellStateComponent}.
         */
        ShellStateComponent empty();

        /**
         * Creates a new instance of {@link ShellStateComponent} that is synced with the player's state.
         * @param player The player.
         * @return The {@linkplain ShellStateComponent}.
         */
        ShellStateComponent of(ServerPlayerEntity player);
    }
}
