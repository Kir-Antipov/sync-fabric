package me.kirantipov.mods.sync.client.gui.controller;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

@Environment(EnvType.CLIENT)
public final class DeathScreenController {
    private static boolean suspended;

    public static boolean isSuspended() {
        return suspended;
    }

    public static void suspend() {
        suspended = true;
    }

    public static void restore() {
        suspended = false;
    }

    static {
        ClientPlayConnectionEvents.JOIN.register((a, b, c) -> restore());
    }
}