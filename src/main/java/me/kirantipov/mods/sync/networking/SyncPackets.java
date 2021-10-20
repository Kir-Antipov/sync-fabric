package me.kirantipov.mods.sync.networking;

import me.kirantipov.mods.sync.api.networking.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public final class SyncPackets {
    public static void init() {
        ServerPlayerPacket.register(SynchronizationRequestPacket.class);
    }

    @Environment(EnvType.CLIENT)
    public static void initClient() {
        ClientPlayerPacket.register(ShellUpdatePacket.class);
        ClientPlayerPacket.register(ShellStateUpdatePacket.class);
        ClientPlayerPacket.register(SynchronizationResponsePacket.class);
        ClientPlayerPacket.register(PlayerIsAlivePacket.class);
        ClientPlayerPacket.register(ShellDestroyedPacket.class);
    }
}
