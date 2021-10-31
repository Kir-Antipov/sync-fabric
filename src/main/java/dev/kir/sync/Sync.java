package dev.kir.sync;

import dev.kir.sync.enery.EnergyRegistry;
import dev.kir.sync.networking.SyncPackets;
import dev.kir.sync.block.SyncBlocks;
import dev.kir.sync.block.entity.SyncBlockEntities;
import dev.kir.sync.client.render.SyncRenderers;
import dev.kir.sync.command.SyncCommands;
import dev.kir.sync.item.SyncItems;
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