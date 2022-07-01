package dev.kir.sync.easteregg.mixin.technoblade;

import dev.kir.sync.easteregg.technoblade.Technoblade;
import dev.kir.sync.easteregg.technoblade.TechnobladeTransformable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.EntityTrackingSoundInstance;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
@Mixin(ClientWorld.class)
abstract class ClientWorldMixin extends World {
    @Shadow
    private @Final MinecraftClient client;

    private ClientWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimension, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, dimension, profiler, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Inject(method = "playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZJ)V", at = @At("HEAD"), cancellable = true)
    private void playSound(double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean useDistance, long seed, CallbackInfo ci) {
        if (category != SoundCategory.NEUTRAL) {
            return;
        }

        List<MobEntity> Technoblades = this.getEntitiesByClass(MobEntity.class, new Box(x - 0.1, y - 0.1, z - 0.1, x + 0.1, y + 0.1, z + 0.1), e -> e instanceof TechnobladeTransformable && ((TechnobladeTransformable)e).isTechnoblade());
        MobEntity entity = Technoblades.size() == 0 ? null : Technoblades.get(0);
        if (entity == null) {
            return;
        }

        Technoblade Technoblade = ((TechnobladeTransformable)entity).asTechnoblade();
        sound = this.getTechnobladeSound(sound);
        if (sound == null) {
            ci.cancel();
            return;
        }

        double distance = this.client.gameRenderer.getCamera().getPos().squaredDistanceTo(x, y, z);
        PositionedSoundInstance positionedSoundInstance = new PositionedSoundInstance(sound, Technoblade.getSoundCategory(), volume, pitch, Random.create(seed), x, y, z);
        if (useDistance && distance > 100) {
            this.client.getSoundManager().play(positionedSoundInstance, (int)(Math.sqrt(distance) * 0.5));
        } else {
            this.client.getSoundManager().play(positionedSoundInstance);
        }
        ci.cancel();
    }

    @Inject(method = "playSoundFromEntity", at = @At("HEAD"), cancellable = true)
    private void playSoundFromEntity(PlayerEntity except, Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch, long seed, CallbackInfo ci) {
        if (except != this.client.player || !(entity instanceof TechnobladeTransformable) || !((TechnobladeTransformable)entity).isTechnoblade()) {
            return;
        }

        Technoblade Technoblade = ((TechnobladeTransformable)entity).asTechnoblade();
        sound = this.getTechnobladeSound(sound);
        if (sound == null) {
            ci.cancel();
            return;
        }

        this.client.getSoundManager().play(new EntityTrackingSoundInstance(sound, Technoblade.getSoundCategory(), volume, pitch, entity, seed));
        ci.cancel();
    }

    private @Nullable SoundEvent getTechnobladeSound(SoundEvent sound) {
        Identifier originalSoundId = sound.getId();
        if (originalSoundId.getPath().endsWith(".ambient") || originalSoundId.getPath().endsWith(".death")) {
            return null;
        }

        SoundEvent fixedSound = Registry.SOUND_EVENT.get(new Identifier(originalSoundId.getNamespace(), originalSoundId.getPath().replaceFirst("\\.[^.]+", ".player")));
        if (fixedSound == null) {
            fixedSound = sound;
        }
        return fixedSound;
    }
}
