package dev.kir.sync.client.render;

import dev.kir.sync.Sync;
import ladysnake.satin.api.managed.ManagedCoreShader;
import ladysnake.satin.api.managed.ShaderEffectManager;
import ladysnake.satin.api.managed.uniform.Uniform1f;
import ladysnake.satin.api.managed.uniform.UniformMat4;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.Matrix4f;

@Environment(EnvType.CLIENT)
public final class CustomGameRenderer extends GameRenderer {
    private static final ManagedCoreShader RENDER_TYPE_ENTITY_TRANSLUCENT_PARTIALLY_TEXTURED_SHADER;
    private static final Uniform1f RENDER_TYPE_ENTITY_TRANSLUCENT_PARTIALLY_TEXTURED_SHADER_UNIFORM_CUTOUT_Y;
    private static final UniformMat4 RENDER_TYPE_ENTITY_TRANSLUCENT_PARTIALLY_TEXTURED_SHADER_UNIFORM_MODEL_MAT;

    private static final ManagedCoreShader RENDER_TYPE_VOXEL_SHADER;

    private CustomGameRenderer(MinecraftClient client, ResourceManager resourceManager, BufferBuilderStorage buffers) {
        super(client, resourceManager, buffers);
    }

    public static void initRenderTypeEntityTranslucentPartiallyTexturedShader(float cutoutY, Matrix4f modelMatrix) {
        RENDER_TYPE_ENTITY_TRANSLUCENT_PARTIALLY_TEXTURED_SHADER_UNIFORM_CUTOUT_Y.set(cutoutY);
        RENDER_TYPE_ENTITY_TRANSLUCENT_PARTIALLY_TEXTURED_SHADER_UNIFORM_MODEL_MAT.set(modelMatrix);
    }

    public static ManagedCoreShader getRenderTypeEntityTranslucentPartiallyTexturedShader() {
        return RENDER_TYPE_ENTITY_TRANSLUCENT_PARTIALLY_TEXTURED_SHADER;
    }

    public static ManagedCoreShader getRenderTypeVoxelShader() {
        return RENDER_TYPE_VOXEL_SHADER;
    }

    public static void initClient() { }

    static {
        RENDER_TYPE_ENTITY_TRANSLUCENT_PARTIALLY_TEXTURED_SHADER = ShaderEffectManager.getInstance().manageCoreShader(Sync.locate("rendertype_entity_translucent_partially_textured"), VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);
        RENDER_TYPE_ENTITY_TRANSLUCENT_PARTIALLY_TEXTURED_SHADER_UNIFORM_CUTOUT_Y = RENDER_TYPE_ENTITY_TRANSLUCENT_PARTIALLY_TEXTURED_SHADER.findUniform1f("CutoutY");
        RENDER_TYPE_ENTITY_TRANSLUCENT_PARTIALLY_TEXTURED_SHADER_UNIFORM_MODEL_MAT = RENDER_TYPE_ENTITY_TRANSLUCENT_PARTIALLY_TEXTURED_SHADER.findUniformMat4("ModelMat");

        RENDER_TYPE_VOXEL_SHADER = ShaderEffectManager.getInstance().manageCoreShader(Sync.locate("rendertype_voxel"), CustomVertexFormats.POSITION_COLOR_OVERLAY_LIGHT_NORMAL);
    }
}