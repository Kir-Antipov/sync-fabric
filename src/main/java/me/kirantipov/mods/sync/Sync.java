package me.kirantipov.mods.sync;

import me.kirantipov.mods.sync.api.enery.EnergyRegistry;
import me.kirantipov.mods.sync.api.networking.SyncPackets;
import me.kirantipov.mods.sync.block.SyncBlocks;
import me.kirantipov.mods.sync.block.entity.SyncBlockEntities;
import me.kirantipov.mods.sync.client.render.SyncRenderers;
import me.kirantipov.mods.sync.command.SyncCommands;
import me.kirantipov.mods.sync.item.SyncItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class Sync implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "sync";

    public static Identifier locate(String location) {
        return new Identifier(MOD_ID, location);
    }

    @Override
    public void onInitialize() {
        SyncBlocks.init();
        SyncBlockEntities.init();
        SyncItems.init();
        EnergyRegistry.init();
        SyncPackets.init();
        SyncCommands.init();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {
        SyncRenderers.initClient();
        SyncPackets.initClient();
    }
}