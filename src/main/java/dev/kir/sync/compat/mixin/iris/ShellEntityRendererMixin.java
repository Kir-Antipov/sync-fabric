package dev.kir.sync.compat.mixin.iris;

import dev.kir.sync.api.shell.ShellState;
import dev.kir.sync.client.render.entity.ShellEntityRenderer;
import dev.kir.sync.client.texture.GeneratedTextureManager;
import dev.kir.sync.client.texture.TextureGenerators;
import dev.kir.sync.compat.iris.IrisRenderLayer;
import dev.kir.sync.entity.ShellEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(ShellEntityRenderer.class)
final class ShellEntityRendererMixin extends PlayerEntityRenderer {
    private ShellEntityRendererMixin(EntityRendererFactory.Context ctx, boolean slim) {
        super(ctx, slim);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumerProvider;getBuffer(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;", ordinal = 0), remap = false)
    private VertexConsumer getBuffer(VertexConsumerProvider vertexConsumers, RenderLayer baseLayer, AbstractClientPlayerEntity player, float yaw, float tickDelta, MatrixStack matrices) {
        VertexConsumer baseConsumer = vertexConsumers.getBuffer(baseLayer);

        ShellEntity shell = (ShellEntity)player;
        float progress = shell.getState().getProgress();
        if (!(progress >= ShellState.PROGRESS_PRINTING && progress < ShellState.PROGRESS_DONE)) {
            return baseConsumer;
        }

        Identifier[] textures = GeneratedTextureManager.getTextures(TextureGenerators.PlayerEntityPartiallyTexturedTextureGenerator);
        if (textures.length == 0) {
            return baseConsumer;
        }

        float printingProgress = (progress - ShellState.PROGRESS_PRINTING) / (ShellState.PROGRESS_PAINTING);
        RenderLayer printingMaskLayer = IrisRenderLayer.getPrintingMask(textures[(int)(textures.length * printingProgress)]);

        // TODO
        // Fix dev.kir.sync.compat.iris.IrisRenderLayer::getPrintingMask,
        // and then fix combining baseConsumer with printingMaskVertexConsumer
        VertexConsumer printingMaskVertexConsumer = vertexConsumers.getBuffer(printingMaskLayer);
        if (printingMaskVertexConsumer == baseConsumer) {
            return vertexConsumers.getBuffer(baseLayer);
        }

        return VertexConsumers.union(printingMaskVertexConsumer, baseConsumer);
    }
}
