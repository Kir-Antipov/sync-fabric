package dev.kir.sync.mixin;

import dev.kir.sync.entity.KillableEntity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
    @Inject(method = "setHealth", at = @At("RETURN"))
    private void setHealth(float health, CallbackInfo ci) {
        if (health <= 0 && this instanceof KillableEntity) {
            ((KillableEntity)this).onKillableEntityDeath();
        }
    }

    @Inject(method = "updatePostDeath", at = @At("HEAD"), cancellable = true)
    private void updatePostDeath(CallbackInfo ci) {
        if (this instanceof KillableEntity && ((KillableEntity)this).updateKillableEntityPostDeath()) {
            ci.cancel();
        }
    }
}
