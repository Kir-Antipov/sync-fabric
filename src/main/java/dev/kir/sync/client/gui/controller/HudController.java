package dev.kir.sync.client.gui.controller;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;

@Environment(EnvType.CLIENT)
public final class HudController {
    private static final GameOptions GAME_OPTIONS = MinecraftClient.getInstance().options;
    private static Boolean wasHudHidden;

    public static void show() {
        setHudHidden(false);
    }

    public static void hide() {
        setHudHidden(true);
    }

    private static void setHudHidden(boolean value) {
        if (wasHudHidden == null) {
            wasHudHidden = GAME_OPTIONS.hudHidden;
        }
        GAME_OPTIONS.hudHidden = value;
    }

    public static void restore() {
        if (wasHudHidden != null) {
            GAME_OPTIONS.hudHidden = wasHudHidden;
            wasHudHidden = null;
        }
    }
}