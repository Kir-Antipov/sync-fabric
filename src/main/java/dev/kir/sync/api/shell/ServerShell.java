package dev.kir.sync.api.shell;

import com.mojang.datafixers.util.Either;
import dev.kir.sync.api.event.PlayerSyncEvents;
import net.minecraft.server.MinecraftServer;

import java.util.Optional;
import java.util.UUID;

/**
 * Server-side version of the {@link Shell}.
 */
public interface ServerShell extends Shell {
    @Override
    default boolean isClient() {
        return false;
    }

    /**
     * Synchronizes the shell with the given state.
     *
     * @param state Target state.
     * @return New state that was generated during the sync process, if it was successful; otherwise, failure reason is returned.
     */
    Either<ShellState, PlayerSyncEvents.SyncFailureReason> sync(ShellState state);

    /**
     * Modifies the internal state of the shell to match the specified one.
     *
     * @param state Target state.
     */
    void apply(ShellState state);


    /**
     * Returns a shell with the given uuid.
     *
     * @param server The server.
     * @param uuid The uuid of the target shell.
     * @return Shell with the given uuid.
     */
    static Optional<ServerShell> getByUuid(MinecraftServer server, UUID uuid) {
        return Optional.ofNullable((ServerShell)server.getPlayerManager().getPlayer(uuid));
    }

    /**
     * Returns a shell with the given name.
     *
     * @param server The server.
     * @param name The name of the target shell.
     * @return Shell with the given name.
     */
    static Optional<ServerShell> getByName(MinecraftServer server, String name) {
        return Optional.ofNullable((ServerShell)server.getPlayerManager().getPlayer(name));
    }
}
