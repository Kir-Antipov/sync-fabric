package me.kirantipov.mods.sync.api.event;

import me.kirantipov.mods.sync.api.core.ShellState;
import me.kirantipov.mods.sync.api.core.ShellStateContainer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Events about the synchronization of {@linkplain PlayerEntity players} with their shells.
 *
 * <p>These events can be categorized into two groups:
 * <ol>
 * <li>Simple listeners: {@link #START_SYNCING} and {@link #STOP_SYNCING}</li>
 * <li>Predicates: {@link #ALLOW_SHELL_CONSTRUCTION}, {@link #ALLOW_SHELL_SELECTION} and {@link #ALLOW_SYNCING}</li>
 * </ol>
 *
 * <p><b>Note:</b> Sync events are fired on both client side and server side.</li>
 */
public final class PlayerSyncEvents {
    /**
     * An event that checks whether a player can start syncing with the given shell.
     *
     * <p>If this event returns a {@link SyncFailureReason}, it is used to fail the syncing process.
     * A null return value means that the player will start syncing.
     *
     * <p>When this event is called, all standard checks have already succeeded, i.e. this event
     * is used in addition to them.
     */
    public static final Event<AllowSyncing> ALLOW_SYNCING = EventFactory.createArrayBacked(AllowSyncing.class, callbacks -> (player, targetState) -> {
        for (AllowSyncing callback : callbacks) {
            SyncFailureReason reason = callback.allowSync(player, targetState);
            if (reason != null) {
                return reason;
            }
        }
        return null;
    });

    /**
     * An event that checks whether a player is able to create a new shell from their sample.
     *
     * <p>If this event returns a {@link ShellConstructionFailureReason}, it is used to fail the construction process.
     * A null return value means that a new shell will be created.
     *
     * <p>When this event is called, all standard checks have already succeeded, i.e. this event
     * is used in addition to them.
     */
    public static final Event<AllowShellConstruction> ALLOW_SHELL_CONSTRUCTION = EventFactory.createArrayBacked(AllowShellConstruction.class, callbacks -> (player, targetContainer) -> {
        for (AllowShellConstruction callback : callbacks) {
            ShellConstructionFailureReason reason = callback.allowShellConstruction(player, targetContainer);
            if (reason != null) {
                return reason;
            }
        }
        return null;
    });

    /**
     * An event that checks whether a player can select a shell to transfer their mind into.
     *
     * <p>If this event returns a {@link ShellSelectionFailureReason}, it is used to fail the selection process.
     * A null return value means that the player will be able to select a shell for the mind transfer process.
     *
     * <p>When this event is called, all standard checks have already succeeded, i.e. this event
     * is used in addition to them.
     */
    public static final Event<AllowShellSelection> ALLOW_SHELL_SELECTION = EventFactory.createArrayBacked(AllowShellSelection.class, callbacks -> (player, targetContainer) -> {
        for (AllowShellSelection callback : callbacks) {
            ShellSelectionFailureReason reason = callback.allowShellSelection(player, targetContainer);
            if (reason != null) {
                return reason;
            }
        }
        return null;
    });


    /**
     * An event that is called when a player starts to sync.
     */
    public static final Event<StartSyncing> START_SYNCING = EventFactory.createArrayBacked(StartSyncing.class, callbacks -> (player, targetState) -> {
        for (StartSyncing callback : callbacks) {
            callback.onStartSyncing(player, targetState);
        }
    });

    /**
     * An event that is called when a player stops syncing and moves to another body.
     */
    public static final Event<StopSyncing> STOP_SYNCING = EventFactory.createArrayBacked(StopSyncing.class, callbacks -> (player, previousPos, storedState) -> {
        for (StopSyncing callback : callbacks) {
            callback.onStopSyncing(player, previousPos, storedState);
        }
    });


    @FunctionalInterface
    public interface SyncFailureReason {
        SyncFailureReason OTHER_PROBLEM = () -> null;
        SyncFailureReason INVALID_SHELL = create(new TranslatableText("event.sync.request.fail.invalid.shell"));
        SyncFailureReason INVALID_CURRENT_LOCATION = create(new TranslatableText("event.sync.request.fail.invalid.location.current"));
        SyncFailureReason INVALID_TARGET_LOCATION = create(new TranslatableText("event.sync.request.fail.invalid.location.target"));

        @Nullable
        Text toText();

        static SyncFailureReason create(@Nullable Text description) {
            return description == null ? OTHER_PROBLEM : () -> description;
        }
    }

    @FunctionalInterface
    public interface ShellConstructionFailureReason {
        ShellConstructionFailureReason OTHER_PROBLEM = () -> null;
        ShellConstructionFailureReason OCCUPIED = create(new TranslatableText("event.sync.construction.fail.occupied"));

        @Nullable
        Text toText();

        static ShellConstructionFailureReason create(@Nullable Text description) {
            return description == null ? OTHER_PROBLEM : () -> description;
        }
    }

    @FunctionalInterface
    public interface ShellSelectionFailureReason {
        ShellSelectionFailureReason OTHER_PROBLEM = () -> null;

        @Nullable
        Text toText();

        static ShellSelectionFailureReason create(@Nullable Text description) {
            return description == null ? OTHER_PROBLEM : () -> description;
        }
    }


    @FunctionalInterface
    public interface AllowSyncing {
        /**
         * Checks whether a player can start syncing with the given shell.
         *
         * @param player the syncing player
         * @param targetState the target shell
         * @return null if the player can sync, or a failure reason if they cannot
         */
        @Nullable
        SyncFailureReason allowSync(PlayerEntity player, ShellState targetState);
    }

    @FunctionalInterface
    public interface AllowShellConstruction {
        /**
         * Checks whether a player is able to create a new shell from their sample.
         *
         * @param player The player.
         * @param targetContainer The shell container that will be used to store the new shell.
         * @return null if the player is able to create a new shell, or a failure reason if they cannot
         */
        @Nullable
        ShellConstructionFailureReason allowShellConstruction(PlayerEntity player, ShellStateContainer targetContainer);
    }

    @FunctionalInterface
    public interface AllowShellSelection {
        /**
         * Checks whether a player can select a shell to transfer their mind into.
         *
         * @param player The player.
         * @param targetContainer The shell container that is currently used by the player.
         * @return null if the player can select a shell to transfer their mind into, or a failure reason if they cannot
         */
        @Nullable
        ShellSelectionFailureReason allowShellSelection(PlayerEntity player, ShellStateContainer targetContainer);
    }


    @FunctionalInterface
    public interface StartSyncing {
        /**
         * Called when a player starts to sync.
         *
         * @param player the syncing player
         * @param targetState the target shell
         */
        void onStartSyncing(PlayerEntity player, ShellState targetState);
    }

    @FunctionalInterface
    public interface StopSyncing {
        /**
         * Called when a player stops syncing and moves to another body.
         *
         * @param player the syncing player
         * @param previousPos a position of the player at the start of synchronization with the current shell
         * @param storedState a previous shell of the player, if any; otherwise, null
         */
        void onStopSyncing(PlayerEntity player, BlockPos previousPos, @Nullable ShellState storedState);
    }


    private PlayerSyncEvents() { }
}
