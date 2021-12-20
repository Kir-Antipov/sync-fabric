package dev.kir.sync.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.function.BiFunction;

@Environment(EnvType.CLIENT)
public final class CustomRenderLayer extends RenderLayer {
    private static final Shader ENTITY_TRANSLUCENT_PARTIALLY_TEXTURED_SHADER;
    private static final Shader VOXEL_SHADER;

    private static final RenderLayer VOXELS;
    private static final BiFunction<Identifier, Boolean, RenderLayer> ENTITY_TRANSLUCENT_PARTIALLY_TEXTURED;

    private CustomRenderLayer(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }

    public static RenderLayer getVoxels() {
        return VOXELS;
    }

    public static RenderLayer getEntityTranslucentPartiallyTextured(Identifier textureId, float cutoutY) {
        return getEntityTranslucentPartiallyTextured(textureId, cutoutY, true);
    }

    public static RenderLayer getEntityTranslucentPartiallyTextured(Identifier textureId, float cutoutY, boolean affectsOutline) {
        net.minecraft.client.render.Shader shader = CustomGameRenderer.getRenderTypeEntityTranslucentPartiallyTexturedShader();
        shader.getUniformOrDefault("CutoutY").set(cutoutY);
        shader.getUniformOrDefault("ModelMat").set(MatrixStackStorage.getModelMatrixStack().peek().getPositionMatrix());
        return ENTITY_TRANSLUCENT_PARTIALLY_TEXTURED.apply(textureId, affectsOutline);
    }

    static {
        ENTITY_TRANSLUCENT_PARTIALLY_TEXTURED_SHADER = new Shader(CustomGameRenderer::getRenderTypeEntityTranslucentPartiallyTexturedShader);
        VOXEL_SHADER = new Shader(CustomGameRenderer::getRenderTypeVoxelShader);

        VOXELS = of("voxels", CustomVertexFormats.POSITION_COLOR_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, false, false, RenderLayer.MultiPhaseParameters.builder().shader(VOXEL_SHADER).transparency(NO_TRANSPARENCY).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(true));
        ENTITY_TRANSLUCENT_PARTIALLY_TEXTURED = Util.memoize((id, outline) -> RenderLayer.of("entity_translucent_partially_textured", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, true, true, RenderLayer.MultiPhaseParameters.builder().shader(ENTITY_TRANSLUCENT_PARTIALLY_TEXTURED_SHADER).texture(new RenderPhase.Texture(id, false, false)).transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY).cull(RenderLayer.DISABLE_CULLING).lightmap(RenderLayer.ENABLE_LIGHTMAP).overlay(RenderLayer.ENABLE_OVERLAY_COLOR).build(outline)));
    }
}