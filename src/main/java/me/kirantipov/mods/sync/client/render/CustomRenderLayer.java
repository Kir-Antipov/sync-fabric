package me.kirantipov.mods.sync.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;

@Environment(EnvType.CLIENT)
public final class CustomRenderLayer extends RenderLayer {
    private static final Shader VOXEL_SHADER;

    private static final RenderLayer VOXELS;

    private CustomRenderLayer(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }

    public static RenderLayer getVoxels() {
        return VOXELS;
    }

    static {
        VOXEL_SHADER = new Shader(CustomGameRenderer::getRenderTypeVoxelShader);

        VOXELS = of("voxels", CustomVertexFormats.POSITION_COLOR_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, false, false, RenderLayer.MultiPhaseParameters.builder().shader(VOXEL_SHADER).transparency(NO_TRANSPARENCY).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(true));
    }
}