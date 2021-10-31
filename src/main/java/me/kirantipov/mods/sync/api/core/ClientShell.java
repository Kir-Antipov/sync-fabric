package me.kirantipov.mods.sync.api.core;

import me.kirantipov.mods.sync.api.event.PlayerSyncEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Client-side version of the {@link Shell}.
 */
@Environment(EnvType.CLIENT)
public interface ClientShell extends Shell {
    @Override
    default boolean isClient() {
        return true;
    }

    /**
     * Begins an asynchronous sync operation.
     *
     * @param state Target state.
     * @return null if the sync process was started; otherwise, a failure reason is returned.
     */
    @Nullable PlayerSyncEvents.SyncFailureReason beginSync(ShellState state);

    /**
     * Handles the end of an asynchronous sync operation.
     *
     * @param startWorld Identifier of the world the sync operation was triggered in.
     * @param startPos Position the sync operation was triggered at.
     * @param startFacing Direction the player was looking at when the sync process started.
     * @param targetWorld Identifier of the target shell's world.
     * @param targetPos Position of the target shell.
     * @param targetFacing Direction the target shell is currently looking at.
     * @param storedState New state that was generated during the sync process, if any; otherwise, null.
     */
    void endSync(Identifier startWorld, BlockPos startPos, Direction startFacing, Identifier targetWorld, BlockPos targetPos, Direction targetFacing, @Nullable ShellState storedState);


    /**
     * @return Main player in the form of {@link ClientShell}.
     */
    static Optional<ClientShell> getMainPlayer() {
        return Optional.ofNullable((ClientShell)MinecraftClient.getInstance().player);
    }
}
