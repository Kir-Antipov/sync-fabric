package me.kirantipov.mods.sync.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface TickableBlockEntity {
    static <T extends BlockEntity> void clientTicker(World world, BlockPos pos, BlockState state, T blockEntity) {
        if (blockEntity instanceof TickableBlockEntity tickable) {
            tickable.onClientTick(world, pos, state);
        }
    }

    static <T extends BlockEntity> void serverTicker(World world, BlockPos pos, BlockState state, T blockEntity) {
        if (blockEntity instanceof TickableBlockEntity tickable) {
            tickable.onServerTick(world, pos, state);
        }
    }

    default void onClientTick(World world, BlockPos pos, BlockState state) {
        this.onTick(world, pos, state);
    }

    default void onServerTick(World world, BlockPos pos, BlockState state) {
        this.onTick(world, pos, state);
    }

    default void onTick(World world, BlockPos pos, BlockState state) { }
}