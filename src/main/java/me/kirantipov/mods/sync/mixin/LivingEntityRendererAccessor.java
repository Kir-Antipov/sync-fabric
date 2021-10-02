package me.kirantipov.mods.sync.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererAccessor<T extends LivingEntity, M extends EntityModel<T>> {
    @Invoker("addFeature")
    boolean executeAddFeature(FeatureRenderer<T, M> featureRenderer);
}
