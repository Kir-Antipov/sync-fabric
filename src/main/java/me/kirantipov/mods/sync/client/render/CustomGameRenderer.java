package me.kirantipov.mods.sync.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Shader;
import net.minecraft.resource.ResourceManager;

@Environment(EnvType.CLIENT)
public final class CustomGameRenderer extends GameRenderer {
    private CustomGameRenderer(MinecraftClient client, ResourceManager resourceManager, BufferBuilderStorage buffers) {
        super(client, resourceManager, buffers);
    }
}