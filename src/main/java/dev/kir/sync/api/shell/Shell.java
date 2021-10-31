package dev.kir.sync.api.shell;

import net.minecraft.util.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * An interface that represents a replicable life form.
 */
public interface Shell extends ShellStateManager {
    /**
     * @return true if the shell exists at the client-side; otherwise, false.
     */
    boolean isClient();

    /**
     * @return UUID of the owner of this shell.
     */
    UUID getShellOwnerUuid();

    /**
     * @return true if this shell is artificial; otherwise, false.
     */
    boolean isArtificial();

    /**
     * Changes artificial status of this shell.
     * @param isArtificial Indicates whether this shell is artificial.
     */
    void changeArtificialStatus(boolean isArtificial);

    @Override
    default boolean isValidShellOwnerUuid(UUID uuid) {
        return uuid == null || uuid.equals(this.getShellOwnerUuid());
    }

    /**
     * Returns true if the given state can be applied to this shell; otherwise, false.
     *
     * @param state The state.
     * @return true if the given state can be applied to this shell; otherwise, false.
     */
    @Contract("null -> false")
    default boolean canBeApplied(ShellState state) {
        return this.isValidShellState(state) && this.getShellOwnerUuid().equals(state.getOwnerUuid());
    }

    /**
     * Returns a shell with the given uuid.
     *
     * @param uuid UUID of the shell.
     * @return Shell with the given uuid, if any; otherwise, null.
     */
    @Nullable
    ShellState getShellStateByUuid(UUID uuid);

    @Override
    @Nullable
    default ShellState getShellStateByUuid(UUID owner, UUID uuid) {
        return this.isValidShellOwnerUuid(owner) ? this.getShellStateByUuid(uuid) : null;
    }

    /**
     * Returns states associated with this shell.
     *
     * @return States associated with this shell.
     */
    Stream<ShellState> getAvailableShellStates();

    @Override
    default Stream<ShellState> getAvailableShellStates(UUID owner) {
        return this.isValidShellOwnerUuid(owner) ? this.getAvailableShellStates() : Stream.of();
    }

    /**
     * Overrides states associated with this shell.
     *
     * @param states The states that should be associated with this shell.
     */
    void setAvailableShellStates(Stream<ShellState> states);

    @Override
    default void setAvailableShellStates(UUID owner, Stream<ShellState> states) {
        if (this.isValidShellOwnerUuid(owner)) {
            this.setAvailableShellStates(states);
        }
    }

    @Override
    default Collection<Pair<ShellStateUpdateType, ShellState>> peekPendingUpdates(UUID owner) {
        return Collections.emptyList();
    }

    @Override
    default void clearPendingUpdates(UUID owner) { }
}