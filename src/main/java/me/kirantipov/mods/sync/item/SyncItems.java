package me.kirantipov.mods.sync.item;

import me.kirantipov.mods.sync.Sync;
import me.kirantipov.mods.sync.block.SyncBlocks;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class SyncItems {
    public static final Item SYNC_CORE;
    public static final Item SHELL_STORAGE;
    public static final Item SHELL_CONSTRUCTOR;
    public static final Item TREADMILL;

    static {
        SYNC_CORE = register("sync_core", new FabricItemSettings().group(SyncItemGroups.MAIN).maxCount(16));
        SHELL_STORAGE = register(SyncBlocks.SHELL_STORAGE, new FabricItemSettings().group(SyncItemGroups.MAIN).maxCount(1));
        SHELL_CONSTRUCTOR = register(SyncBlocks.SHELL_CONSTRUCTOR, new FabricItemSettings().group(SyncItemGroups.MAIN).maxCount(1));
        TREADMILL = register(SyncBlocks.TREADMILL, new FabricItemSettings().group(SyncItemGroups.MAIN).maxCount(1));
    }

    public static void init() { }

    private static Item register(String id, Item.Settings settings) {
        Identifier trueId = Sync.locate(id);
        Item item = new Item(settings);
        return Registry.register(Registry.ITEM, trueId, item);
    }

    private static Item register(Block block, Item.Settings settings) {
        Identifier id = Registry.BLOCK.getId(block);
        BlockItem item = new BlockItem(block, settings);
        item.appendBlocks(Item.BLOCK_ITEMS, item);
        return Registry.register(Registry.ITEM, id, item);
    }
}
