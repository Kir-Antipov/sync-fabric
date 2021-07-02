package me.kirantipov.mods.sync.api.core;

import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * The shell state manager is an interface that allows applications
 * to implement a container for shell states.
 */
public interface ShellStateManager {
    /**
     * Overrides shells associated with the given owner.
     *
     * @param owner The owner of the shells.
     * @param states The shells that should be associated with the owner.
     */
    void setAvailableShellStates(UUID owner, Stream<ShellState> states);

    /**
     * Returns shells associated with the given owner.
     *
     * @param owner The owner of the shells.
     * @return Shells associated with the given owner.
     */
    Stream<ShellState> getAvailableShellStates(UUID owner);

    /**
     * Returns a shell with the given uuid.
     *
     * @param owner The owner of the shell.
     * @param uuid UUID of the shell.
     * @return Shell with the given uuid, if any; otherwise, null.
     */
    @Nullable
    ShellState getShellStateByUuid(UUID owner, UUID uuid);

    /**
     * Adds the shell to the container.
     * @param state The shell.
     */
    void add(ShellState state);

    /**
     * Removes the shell from the container.
     * @param state The shell.
     */
    void remove(ShellState state);

    /**
     * Updates the given shell.
     * @param state The shell.
     */
    void update(ShellState state);

    /**
     * Updates state of the given shell.
     *
     * @param type Type of the update.
     * @param state The shell.
     */
    default void update(ShellStateUpdateType type, ShellState state) {
        switch (type) {
            case ADD:
                this.add(state);
                break;
            case UPDATE:
                this.update(state);
                break;
            case REMOVE:
                this.remove(state);
                break;
            default:
                break;
        }
    }

    /**
     * Returns true if the manager can accept shells with the given owner uuid;
     * otherwise, false.
     *
     * @param uuid UUID of the shell owner.
     * @return true if the manager can accept shells with the given owner uuid; otherwise, false.
     */
    default boolean isValidShellOwnerUuid(UUID uuid) {
        return uuid != null;
    }

    /**
     * Returns true if the manager is capable of storing the given shell; otherwise, false.
     *
     * @param state The shell.
     * @return true if the manager is capable of storing the given shell; otherwise, false.
     */
    default boolean isValidShellState(ShellState state) {
        return state != null && state.getUuid() != null && state.getOwnerUuid() != null;
    }

    /**
     * Returns a list of pending updates by owner uuid.
     *
     * @param owner UUID of the shell owner.
     * @return A list of pending updates by owner uuid.
     */
    Collection<Pair<ShellStateUpdateType, ShellState>> peekPendingUpdates(UUID owner);

    /**
     * Clears all pending updates for the given shell owner.
     * @param owner UUID of the shell owner.
     */
    void clearPendingUpdates(UUID owner);

    /**
     * Clears all pending updates for the given shell owner.
     *
     * @param owner UUID of the shell owner.
     * @return A list of pending updates by owner uuid.
     */
    default Collection<Pair<ShellStateUpdateType, ShellState>> popPendingUpdates(UUID owner) {
        Collection<Pair<ShellStateUpdateType, ShellState>> updates = this.peekPendingUpdates(owner);
        this.clearPendingUpdates(owner);
        return updates;
    }
}