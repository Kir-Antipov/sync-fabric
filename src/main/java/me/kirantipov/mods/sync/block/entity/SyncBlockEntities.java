package me.kirantipov.mods.sync.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class SyncBlockEntities {

    public static void init() { }

    private static <T extends BlockEntity> BlockEntityType<T> register(BlockEntityType.BlockEntityFactory<T> factory, Block block) {
        Identifier id = Registry.BLOCK.getId(block);
        return Registry.register(Registry.BLOCK_ENTITY_TYPE, id, BlockEntityType.Builder.create(factory, block).build(null));
    }
}
