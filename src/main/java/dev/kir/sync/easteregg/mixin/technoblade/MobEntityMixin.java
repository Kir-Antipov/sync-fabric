package dev.kir.sync.easteregg.mixin.technoblade;

import dev.kir.sync.Sync;
import dev.kir.sync.easteregg.technoblade.TechnobladeTransformable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MobEntity.class)
abstract class MobEntityMixin extends LivingEntity {
    private MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(CallbackInfo ci) {
        if (!(this instanceof TechnobladeTransformable) || !((TechnobladeTransformable)this).isTechnoblade()) {
            return;
        }

        ((TechnobladeTransformable)this).asTechnoblade().tick();
        if (this.dead) {
            Sync.getConfig().removeTechnoblade(this.uuid);
        }
    }
}
