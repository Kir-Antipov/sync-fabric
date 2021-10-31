package dev.kir.sync.item;

import dev.kir.sync.Sync;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public final class SyncItemGroups {
    public static final ItemGroup MAIN = FabricItemGroupBuilder.build(
        Sync.locate(Sync.MOD_ID),
        () -> new ItemStack(SyncItems.SYNC_CORE)
    );
}