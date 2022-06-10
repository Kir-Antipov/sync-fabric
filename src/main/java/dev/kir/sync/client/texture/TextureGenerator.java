package dev.kir.sync.client.texture;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.AbstractTexture;

import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
@FunctionalInterface
public interface TextureGenerator {
    Stream<AbstractTexture> generateTextures();
}
