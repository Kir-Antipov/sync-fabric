package dev.kir.sync.client.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public final class GeneratedTextureManager {
    private static final Map<TextureGenerator, Identifier[]> GENERATED_TEXTURES = new HashMap<>();
    private static final Identifier[] EMPTY_TEXTURES = new Identifier[0];

    public static Identifier[] getTextures(TextureGenerator generator) {
        Identifier[] textures = GENERATED_TEXTURES.get(generator);
        if (textures == null) {
            if (RenderSystem.isOnRenderThreadOrInit()) {
                textures = genTextures(generator, GENERATED_TEXTURES.size());
                GENERATED_TEXTURES.put(generator, textures);
            } else {
                RenderSystem.recordRenderCall(() -> GENERATED_TEXTURES.put(generator, genTextures(generator, GENERATED_TEXTURES.size())));
                textures = EMPTY_TEXTURES;
            }
        }
        return textures;
    }

    private static Identifier[] genTextures(TextureGenerator generator, int generatorId) {
        RenderSystem.assertOnRenderThreadOrInit();

        int textureCounter = -1;
        String format = generator.getClass().getSimpleName().toLowerCase() + "_" + generatorId + "_";
        TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
        List<Identifier> textures = new ArrayList<>();
        for (AbstractTexture texture : (Iterable<AbstractTexture>)generator.generateTextures()::iterator) {
            Identifier id = new Identifier("__dynamic", format + (++textureCounter));
            textureManager.registerTexture(id, texture);
            textures.add(id);
        }

        return textures.toArray(new Identifier[0]);
    }
}
