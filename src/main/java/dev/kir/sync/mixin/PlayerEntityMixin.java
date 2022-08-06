package dev.kir.sync.mixin;

import dev.kir.sync.api.shell.Shell;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
abstract class PlayerEntityMixin extends LivingEntity {
    @Final
    @Shadow
    private PlayerInventory inventory;

    @Shadow
    public int experienceLevel;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    protected abstract void vanishCursedItems();

    @Shadow
    public abstract boolean isSpectator();

    @Inject(method = "dropInventory", at = @At("RETURN"))
    private void forceDropInventory(CallbackInfo ci) {
        if (this instanceof Shell shell && shell.isArtificial() && !this.isSpectator() && this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            this.vanishCursedItems();
            this.inventory.dropAll();
        }
    }

    @Inject(method = "getXpToDrop", at = @At("RETURN"), cancellable = true)
    private void forceDropXp(PlayerEntity player, CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValue() == 0 && this instanceof Shell shell && shell.isArtificial() && !this.isSpectator() && this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            cir.setReturnValue(Math.min(this.experienceLevel * 7, 100));
        }
    }
}
