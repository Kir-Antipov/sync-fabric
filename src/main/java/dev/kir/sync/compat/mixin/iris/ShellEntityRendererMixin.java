package dev.kir.sync.compat.mixin.iris;

import dev.kir.sync.api.shell.ShellState;
import dev.kir.sync.client.render.entity.ShellEntityRenderer;
import dev.kir.sync.client.texture.GeneratedTextureManager;
import dev.kir.sync.client.texture.TextureGenerators;
import dev.kir.sync.compat.iris.IrisRenderLayer;
import dev.kir.sync.entity.ShellEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ShellEntityRenderer.class)
final class ShellEntityRendererMixin extends PlayerEntityRenderer {
    private ShellEntityRendererMixin(EntityRendererFactory.Context ctx, boolean slim) {
        super(ctx, slim);
    }

    @Inject(method = "getVertexConsumerForPartiallyTexturedEntity", at = @At("RETURN"), cancellable = true)
    private void getVertexConsumerForPartiallyTexturedEntity(ShellEntity shellEntity, float progress, RenderLayer baseLayer, VertexConsumerProvider vertexConsumers, CallbackInfoReturnable<VertexConsumer> cir) {
        VertexConsumer baseConsumer = cir.getReturnValue();

        if (!(progress >= ShellState.PROGRESS_PRINTING && progress < ShellState.PROGRESS_DONE)) {
            return;
        }

        Identifier[] textures = GeneratedTextureManager.getTextures(TextureGenerators.PlayerEntityPartiallyTexturedTextureGenerator);
        if (textures.length == 0) {
            return;
        }

        float printingProgress = (progress - ShellState.PROGRESS_PRINTING) / (ShellState.PROGRESS_PAINTING);
        RenderLayer printingMaskLayer = IrisRenderLayer.getPrintingMask(textures[(int)(textures.length * printingProgress)]);

        // TODO
        // Fix dev.kir.sync.compat.iris.IrisRenderLayer::getPrintingMask,
        // and then fix combining baseConsumer with printingMaskVertexConsumer
        VertexConsumer printingMaskVertexConsumer = vertexConsumers.getBuffer(printingMaskLayer);
        if (printingMaskVertexConsumer == baseConsumer) {
            cir.setReturnValue(vertexConsumers.getBuffer(baseLayer));
            return;
        }

        cir.setReturnValue(VertexConsumers.union(printingMaskVertexConsumer, baseConsumer));
    }
}
