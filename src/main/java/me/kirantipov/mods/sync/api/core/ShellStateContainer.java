package me.kirantipov.mods.sync.api.core;

import me.kirantipov.mods.sync.Sync;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * A container that can store player's shell.
 */
public interface ShellStateContainer {
    /**
     * An object that allows retrieving {@link ShellStateContainer}s from blocks in a world.
     */
    BlockApiLookup<ShellStateContainer, @Nullable ShellState> LOOKUP = BlockApiLookup.get(Sync.locate("shell_container"), ShellStateContainer.class, ShellState.class);


    /**
     * Attempts to retrieve a {@link ShellStateContainer} instance from a block in the world.
     *
     * @param world The world.
     * @param pos The position of the block.
     * @return The {@linkplain ShellStateContainer} available at the given position, if any; otherwise, null.
     */
    @Nullable
    static ShellStateContainer find(World world, BlockPos pos) {
        return LOOKUP.find(world, pos, null);
    }

    /**
     * Attempts to retrieve a {@link ShellStateContainer} that contains a given {@link ShellState}.
     *
     * @param world The world.
     * @param state The {@linkplain ShellState}.
     * @return The {@linkplain ShellStateContainer} that contains the given {@linkplain ShellState}, if any; otherwise, null.
     */
    @Nullable
    static ShellStateContainer find(World world, ShellState state) {
        return LOOKUP.find(world, state.getPos(), state);
    }


    /**
     * Indicates whether {@link ShellState} that is stored in this container is available for remote use
     * (i.e., should be displayed in player's radial menu).
     *
     * @return true if the {@linkplain ShellState} that is stored in this container
     * is available for remote use and should be displayed in player's radial menu;
     * otherwise, false.
     */
    default boolean isRemotelyAccessible() {
        return true;
    }

    /**
     * @return {@link ShellState} that is currently stored in the container, if any;
     * otherwise, null.
     */
    @Nullable
    ShellState getShellState();

    /**
     * Stores the given {@link ShellState} in the container.
     * @param state The {@linkplain ShellState}.
     */
    void setShellState(@Nullable ShellState state);

    /**
     * @return Color of the container, if any; otherwise, null.
     */
    @Nullable
    default DyeColor getColor() {
        ShellState state = this.getShellState();
        return state == null ? null : state.getColor();
    }
}
