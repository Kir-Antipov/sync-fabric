package me.kirantipov.mods.sync.util.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.DyeColor;

@Environment(EnvType.CLIENT)
public final class ColorUtil {
    public static int fromDyeColor(DyeColor color) {
        return fromDyeColor(color, 1F);
    }

    public static int fromDyeColor(DyeColor color, float a) {
        float[] rgb = color.getColorComponents();
        return fromRGBA(rgb[0], rgb[1], rgb[2], a);
    }

    public static int fromRGBA(float r, float g, float b, float a) {
        return ((byte)(a * 255) << 24) + ((byte)(r * 255) << 16) + ((byte)(g * 255) << 8) + (byte)(b * 255);
    }

    public static float[] toRGBA(int color) {
        float a = ((color >> 24) & 255) / 255F;
        float r = ((color >> 16) & 255) / 255F;
        float g = ((color >> 8) & 255) / 255F;
        float b = (color & 255) / 255F;

        return new float[] { r, g, b, a };
    }
}
