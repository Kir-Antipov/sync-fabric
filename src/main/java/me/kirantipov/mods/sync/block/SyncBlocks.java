package me.kirantipov.mods.sync.block;

import me.kirantipov.mods.sync.Sync;
import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;

public final class SyncBlocks {
    public static final Block TREADMILL;

    static {
        TREADMILL = register("treadmill", new TreadmillBlock(AbstractBlock.Settings.of(Material.STONE, DyeColor.GRAY).requiresTool().strength(1.8F).allowsSpawning(SyncBlocks::never).solidBlock(SyncBlocks::never).suffocates(SyncBlocks::never).blockVision(SyncBlocks::never)));
    }

    public static void init() { }

    private static Block register(String id, Block block) {
        Identifier trueId = Sync.locate(id);
        return Registry.register(Registry.BLOCK, trueId, block);
    }

    private static boolean never(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    private static Boolean never(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) {
        return false;
    }
}
