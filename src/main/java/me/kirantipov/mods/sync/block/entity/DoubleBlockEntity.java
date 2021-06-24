package me.kirantipov.mods.sync.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.DoubleBlockProperties;

public interface DoubleBlockEntity {
    DoubleBlockProperties.Type getBlockType(BlockState state);
}
