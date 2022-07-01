package dev.kir.sync.easteregg.mixin.technoblade;

import dev.kir.sync.easteregg.technoblade.Technoblade;
import dev.kir.sync.easteregg.technoblade.TechnobladeManager;
import dev.kir.sync.easteregg.technoblade.TechnobladeTransformable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(PigEntity.class)
abstract class PigEntityMixin implements TechnobladeTransformable {
    private Technoblade Technoblade = null;

    @Inject(method = "getAmbientSound", at = @At("HEAD"), cancellable = true)
    private void getAmbientSound(CallbackInfoReturnable<SoundEvent> cir) {
        if (this.Technoblade != null) {
            cir.setReturnValue(SoundEvents.ENTITY_PLAYER_BREATH);
        }
    }

    @Inject(method = "getHurtSound", at = @At("HEAD"), cancellable = true)
    private void getHurtSound(DamageSource source, CallbackInfoReturnable<SoundEvent> cir) {
        if (this.Technoblade != null) {
            cir.setReturnValue(this.Technoblade.getHurtSound(source));
        }
    }

    @Inject(method = "getDeathSound", at = @At("HEAD"), cancellable = true)
    private void getDeathSound(CallbackInfoReturnable<SoundEvent> cir) {
        if (this.Technoblade != null) {
            cir.setReturnValue(this.Technoblade.getDeathSound());
        }
    }

    @Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
    private void playStepSound(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (this.Technoblade != null) {
            this.Technoblade.playStepSound(pos, state);
            ci.cancel();
        }
    }

    public Technoblade asTechnoblade() {
        if (this.Technoblade == null) {
            TechnobladeManager.refreshTechnobladeStatus((Entity)(Object)this);
        }
        return this.Technoblade;
    }

    public boolean transformIntoTechnoblade() {
        if (this.Technoblade != null) {
            return false;
        }

        // No, Java, I'm not gonna change this to lowercase
        this.Technoblade = dev.kir.sync.easteregg.technoblade.Technoblade.from((LivingEntity)(Object)this);
        return this.Technoblade != null;
    }
}
