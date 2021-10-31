package me.kirantipov.mods.sync.api.shell;

/**
 * Represents the type of shell update.
 */
public enum ShellStateUpdateType {
    /**
     * There was no update.
     */
    NONE,

    /**
     * New shell was added to the storage.
     */
    ADD,

    /**
     * Existing shell was updated.
     */
    UPDATE,

    /**
     * Existing shell was removed from the storage.
     */
    REMOVE
}