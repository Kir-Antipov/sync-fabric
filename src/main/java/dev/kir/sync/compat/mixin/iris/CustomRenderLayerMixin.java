package dev.kir.sync.compat.mixin.iris;

import dev.kir.sync.client.render.CustomRenderLayer;
import dev.kir.sync.compat.iris.IrisRenderLayer;
import ladysnake.satin.api.managed.ManagedCoreShader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(CustomRenderLayer.class)
final class CustomRenderLayerMixin {
    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lladysnake/satin/api/managed/ManagedCoreShader;getRenderLayer(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/RenderLayer;", ordinal = 0, remap = false))
    private static RenderLayer initVoxelRenderLayer(ManagedCoreShader shader, RenderLayer baseLayer) {
        return IrisRenderLayer.getVoxels();
    }

    /**
     * @author Me
     * @reason It's my method, I know what I'm doing
     */
    @Overwrite(remap = false)
    public static RenderLayer getEntityTranslucentPartiallyTextured(Identifier textureId, float cutoutY, boolean affectsOutline) {
        return IrisRenderLayer.getEntityTranslucentPartiallyTextured(textureId, cutoutY, affectsOutline);
    }
}
