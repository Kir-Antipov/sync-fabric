package dev.kir.sync.mixin;

import com.google.common.collect.ImmutableMap;
import dev.kir.sync.client.render.entity.ShellEntityRenderer;
import dev.kir.sync.entity.ShellEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.impl.client.rendering.RegistrationHelperImpl;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Environment(EnvType.CLIENT)
@Mixin(EntityRenderDispatcher.class)
abstract class EntityRenderDispatcherMixin {
    @Shadow
    private @Final ItemRenderer itemRenderer;

    @Shadow
    private @Final TextRenderer textRenderer;

    @Shadow
    private @Final EntityModelLoader modelLoader;

    @Shadow
    private @Final HeldItemRenderer heldItemRenderer;

    @Shadow
    private @Final BlockRenderManager blockRenderManager;

    @Unique
    private Map<String, EntityRenderer<? extends PlayerEntity>> shellRenderers = ImmutableMap.of();

    @SuppressWarnings("unchecked")
    @Inject(method = "getRenderer", at = @At("HEAD"), cancellable = true)
    private <T extends Entity> void getRenderer(T entity, CallbackInfoReturnable<EntityRenderer<? super T>> cir) {
        if (entity instanceof ShellEntity shell) {
            EntityRenderer<? extends PlayerEntity> renderer = this.shellRenderers.get(shell.getModel());
            if (renderer != null) {
                cir.setReturnValue((EntityRenderer<? super T>)renderer);
            }
        }
    }

    @Inject(method = "reload", at = @At("HEAD"))
    private void reload(ResourceManager manager, CallbackInfo ci) {
        EntityRendererFactory.Context context = new EntityRendererFactory.Context((EntityRenderDispatcher)(Object)this, this.itemRenderer, this.blockRenderManager, this.heldItemRenderer, manager, this.modelLoader, this.textRenderer);
        this.shellRenderers = ImmutableMap.of(
            "default", createShellEntityRenderer(context, false),
            "slim", createShellEntityRenderer(context, true)
        );
    }

    @SuppressWarnings({"unchecked", "ConstantConditions", "rawtypes"})
    private static ShellEntityRenderer createShellEntityRenderer(EntityRendererFactory.Context context, boolean slim) {
        ShellEntityRenderer shellEntityRenderer = new ShellEntityRenderer(context, slim);
        LivingEntityRendererAccessor accessor = (LivingEntityRendererAccessor)shellEntityRenderer;
        LivingEntityFeatureRendererRegistrationCallback.EVENT.invoker().registerRenderers(EntityType.PLAYER, shellEntityRenderer, new RegistrationHelperImpl(accessor::executeAddFeature), context);
        return shellEntityRenderer;
    }
}