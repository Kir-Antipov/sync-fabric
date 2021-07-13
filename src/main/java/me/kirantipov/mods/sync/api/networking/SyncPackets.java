package me.kirantipov.mods.sync.api.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public final class SyncPackets {
    public static void init() {
    }

    @Environment(EnvType.CLIENT)
    public static void initClient() {
        ClientPlayerPacket.register(ShellDestroyedPacket.class);
    }
}
