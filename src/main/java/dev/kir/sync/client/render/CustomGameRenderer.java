package dev.kir.sync.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Shader;
import net.minecraft.resource.ResourceManager;

@Environment(EnvType.CLIENT)
public final class CustomGameRenderer extends GameRenderer {
    private static Shader renderTypeEntityTranslucentPartiallyTexturedShader = null;
    private static Shader renderTypeVoxelShader = null;

    private CustomGameRenderer(MinecraftClient client, ResourceManager resourceManager, BufferBuilderStorage buffers) {
        super(client, resourceManager, buffers);
    }

    public static void setRenderTypeEntityTranslucentPartiallyTexturedShader(Shader shader) {
        renderTypeEntityTranslucentPartiallyTexturedShader = shader;
    }

    public static Shader getRenderTypeEntityTranslucentPartiallyTexturedShader() {
        return renderTypeEntityTranslucentPartiallyTexturedShader;
    }

    public static void setRenderTypeVoxelShader(Shader shader) {
        renderTypeVoxelShader = shader;
    }

    public static Shader getRenderTypeVoxelShader() {
        return renderTypeVoxelShader;
    }
}