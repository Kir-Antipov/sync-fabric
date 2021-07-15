package me.kirantipov.mods.sync.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.*;
import net.minecraft.world.dimension.DimensionType;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class PersistentCameraEntityGoal {
    public static final double MAX_DISTANCE = 25;
    public static final long PHASE_DELAY = 200;
    public static final double MAX_Y = DimensionType.OVERWORLD.getHeight() * 1.01;
    public static final long MIN_PHASE_DURATION = 400;
    public static final long MAX_PHASE_DURATION = 2500;

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

    public static PersistentCameraEntityGoal stairwayToHeaven(BlockPos start, Direction startFacing, BlockPos target, Consumer<PersistentCameraEntity> onTransitionFinished) {
        return stairwayToHeaven(start, startFacing, MAX_Y, target, MIN_PHASE_DURATION, MAX_PHASE_DURATION, PHASE_DELAY, MAX_DISTANCE, onTransitionFinished);
    }

    public static PersistentCameraEntityGoal stairwayToHeaven(BlockPos start, Direction startFacing, double y, BlockPos target, long firstPhaseDuration, long secondPhaseDuration, long phaseDelay, double maxDistance, Consumer<PersistentCameraEntity> onTransitionFinished) {
        double dX = target.getX() - start.getX();
        double dZ = target.getZ() - start.getZ();
        double horizontalDistance = Math.sqrt(dX * dX + dZ * dZ);
        if (horizontalDistance > maxDistance) {
            double factor = maxDistance / horizontalDistance;
            target = new BlockPos(start.add(dX * factor, 0, dZ * factor));
        }

        float yaw = startFacing.asRotation();
        float pitch = 90;
        BlockPos pos0 = start.offset(startFacing.getOpposite());
        Vec3d pos1 = new Vec3d(start.getX() + target.getX(), y * 2, start.getZ() + target.getZ()).multiply(0.5);

        PersistentCameraEntityGoal goal0 = create(pos0, yaw, 0, firstPhaseDuration);
        PersistentCameraEntityGoal goal1 = create(pos1, yaw, pitch, phaseDelay, secondPhaseDuration, onTransitionFinished);

        return goal0.then(goal1);
    }
}