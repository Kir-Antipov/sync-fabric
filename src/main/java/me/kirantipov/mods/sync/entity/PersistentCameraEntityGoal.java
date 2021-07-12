package me.kirantipov.mods.sync.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.*;
import net.minecraft.world.dimension.DimensionType;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class PersistentCameraEntityGoal {
    public final Vec3d pos;
    public final float yaw;
    public final float pitch;
    public final long delay;
    public final long duration;
    private final Consumer<PersistentCameraEntity> onTransitionFinished;

    private PersistentCameraEntityGoal(Vec3d pos, float yaw, float pitch, long delay, long duration, Consumer<PersistentCameraEntity> onTransitionFinished) {
        this.pos = pos;
        this.yaw = yaw;
        this.pitch = pitch;
        this.delay = delay;
        this.duration = duration;
        this.onTransitionFinished = onTransitionFinished;
    }

    public void finish(PersistentCameraEntity camera) {
        if (this.onTransitionFinished != null) {
            this.onTransitionFinished.accept(camera);
        }
    }

    public PersistentCameraEntityGoal then(PersistentCameraEntityGoal nextGoal) {
        return this.then(camera -> camera.setGoal(nextGoal));
    }

    public PersistentCameraEntityGoal then(Consumer<PersistentCameraEntity> callback) {
        Consumer<PersistentCameraEntity> combined = callback == null ? this.onTransitionFinished : this.onTransitionFinished == null ? callback : this.onTransitionFinished.andThen(callback);
        return new PersistentCameraEntityGoal(this.pos, this.yaw, this.pitch, this.delay, this.duration, combined);
    }


    public static PersistentCameraEntityGoal create(BlockPos pos, float yaw, float pitch, long duration) {
        return create(pos, yaw, pitch, 0, duration, null);
    }

    public static PersistentCameraEntityGoal create(BlockPos pos, float yaw, float pitch, long delay, long duration, Consumer<PersistentCameraEntity> onTransitionFinished) {
        Vec3d vecPos = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        return create(vecPos, yaw, pitch, delay, duration, onTransitionFinished);
    }

    public static PersistentCameraEntityGoal create(Vec3d pos, float yaw, float pitch, long duration) {
        return create(pos, yaw, pitch, 0, duration, null);
    }

    public static PersistentCameraEntityGoal create(Vec3d pos, float yaw, float pitch, long delay, long duration, Consumer<PersistentCameraEntity> onTransitionFinished) {
        return new PersistentCameraEntityGoal(pos, yaw, pitch, delay, duration, onTransitionFinished);
    }
}