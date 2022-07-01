package dev.kir.sync.easteregg.mixin.technoblade;

import dev.kir.sync.easteregg.technoblade.Technoblade;
import dev.kir.sync.easteregg.technoblade.TechnobladeTransformable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MobEntityRenderer.class)
abstract class MobEntityRendererMixin {
    @Inject(method = "render(Lnet/minecraft/entity/mob/MobEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    private void render(MobEntity mobEntity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!(mobEntity instanceof TechnobladeTransformable technobladeTransformable) || !technobladeTransformable.isTechnoblade()) {
            return;
        }

        Technoblade Technoblade = technobladeTransformable.asTechnoblade();
        Technoblade.copyPose(mobEntity);
        EntityRenderDispatcher renderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        renderDispatcher.render(Technoblade, 0, 0, 0, yaw, tickDelta, matrices, vertexConsumers, light);
        ci.cancel();
    }
}
