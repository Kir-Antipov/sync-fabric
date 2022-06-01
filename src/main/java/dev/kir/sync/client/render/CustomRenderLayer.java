package dev.kir.sync.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.function.BiFunction;

@Environment(EnvType.CLIENT)
public final class CustomRenderLayer extends RenderLayer {
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
        CustomGameRenderer.initRenderTypeEntityTranslucentPartiallyTexturedShader(cutoutY, MatrixStackStorage.getModelMatrixStack().peek().getPositionMatrix());
        return ENTITY_TRANSLUCENT_PARTIALLY_TEXTURED.apply(textureId, affectsOutline);
    }

    static {
        VOXELS = CustomGameRenderer.getRenderTypeVoxelShader().getRenderLayer(of("voxels", CustomVertexFormats.POSITION_COLOR_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, false, false, RenderLayer.MultiPhaseParameters.builder().shader(RenderPhase.SOLID_SHADER).transparency(NO_TRANSPARENCY).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(true)));
        ENTITY_TRANSLUCENT_PARTIALLY_TEXTURED = Util.memoize((id, outline) -> CustomGameRenderer.getRenderTypeEntityTranslucentPartiallyTexturedShader().getRenderLayer(RenderLayer.getEntityTranslucent(id, outline)));
    }
}