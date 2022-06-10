package dev.kir.sync.compat.iris;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class IrisRenderLayer extends RenderLayer {
    private static final RenderLayer VOXELS;

    private IrisRenderLayer(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }

    public static RenderLayer getVoxels() {
        return VOXELS;
    }

    @SuppressWarnings("unused")
    public static RenderLayer getEntityTranslucentPartiallyTextured(Identifier textureId, float cutoutY, boolean affectsOutline) {
        return RenderLayer.getEntityTranslucent(textureId, affectsOutline);
    }

    public static RenderLayer getPrintingMask(Identifier textureId) {
        // Printing mask needs something like RenderLayer.getOutline,
        // but with proper color/depth test
        return RenderLayer.getEntityTranslucent(textureId);
    }

    static {
        VOXELS = RenderLayer.getEntitySolid(new Identifier("textures/block/white_concrete.png"));
    }
}
