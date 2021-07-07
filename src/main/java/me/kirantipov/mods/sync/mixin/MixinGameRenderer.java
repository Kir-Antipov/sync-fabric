package me.kirantipov.mods.sync.mixin;

import com.google.common.collect.ImmutableList;
import me.kirantipov.mods.sync.client.render.CustomGameRenderer;
import me.kirantipov.mods.sync.client.render.CustomVertexFormats;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import oshi.util.tuples.Triplet;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Unique
    private static final ImmutableList<Triplet<String, VertexFormat, Consumer<Shader>>> CUSTOM_SHADERS = ImmutableList.of(
        new Triplet<String, VertexFormat, Consumer<Shader>>("rendertype_entity_translucent_partially_textured", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, CustomGameRenderer::setRenderTypeEntityTranslucentPartiallyTexturedShader),
        new Triplet<String, VertexFormat, Consumer<Shader>>("rendertype_voxel", CustomVertexFormats.POSITION_COLOR_OVERLAY_LIGHT_NORMAL, CustomGameRenderer::setRenderTypeVoxelShader)
    );

    @Final
    @Shadow
    private Map<String, Shader> shaders;

    @Inject(method = "loadShaders", at = @At("TAIL"))
    private void loadCustomShaders(ResourceManager manager, CallbackInfo ci) throws IOException {
        for (Triplet<String, VertexFormat, Consumer<Shader>> entry : CUSTOM_SHADERS) {
            Shader shader = new Shader(manager, entry.getA(), entry.getB());
            this.shaders.put(shader.getName(), shader);
            entry.getC().accept(shader);
        }
    }
}