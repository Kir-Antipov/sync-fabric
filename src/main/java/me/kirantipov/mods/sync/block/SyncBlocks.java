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

    public static void init() { }

    private static Block register(String id, Block block) {
        Identifier trueId = Sync.locate(id);
        return Registry.register(Registry.BLOCK, trueId, block);
    }
}
