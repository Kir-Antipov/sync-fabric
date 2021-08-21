package me.kirantipov.mods.sync.block.entity;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class TreadmillStateManager {
    private static final int MAX_RUNNING_TIME = 20 * 60 * 15; // ticks -> seconds -> minutes
    private static final double MAX_SQUARED_DISTANCE = 0.5;
    private static final double MAX_DIAGONAL_DISTANCE_HALF = Math.sqrt(3) * Math.sqrt(MAX_SQUARED_DISTANCE);
    private static final ImmutableMap<Class<? extends Entity>, Float> ENERGY_MAP = createEnergyMap();

    private Entity runner;
    private int runningTime;

    public Entity getRunner() {
        return this.runner;
    }

    public boolean setRunner(Entity runner) {
        if (this.runner != runner) {
            if (runner == null) {
                this.runningTime = 0;
            }
            this.runner = runner;
            return true;
        }
        return false;
    }

    public int getRunningTime() {
        return this.runningTime;
    }

    public void setRunningTime(int runningTime) {
        this.runningTime = MathHelper.clamp(runningTime, 0, this.getMaxRunningTime());
    }

    public int getMaxRunningTime() {
        return MAX_RUNNING_TIME;
    }

    public boolean isOverheated() {
        return this.getRunner() != null && this.getRunningTime() >= this.getMaxRunningTime();
    }

    public void tick(Vec3d pos, Direction face) {
        if (this.runner == null) {
            return;
        }

        this.runningTime = Math.min(++this.runningTime, this.getMaxRunningTime());
        if (this.runner.world.isClient) {
            if (this.runner instanceof LivingEntity livingEntity) {
                livingEntity.limbDistance = 1.5F + 2F * runningTime / this.getMaxRunningTime();
            }
        } else {
            float yaw = face.asRotation();
            this.runner.updatePositionAndAngles(pos.x, pos.y, pos.z, yaw, 0);
            this.runner.setHeadYaw(yaw);
            this.runner.setBodyYaw(yaw);
            this.runner.setYaw(yaw);
            this.runner.prevYaw = yaw;
            if (this.runner instanceof LivingEntity livingEntity) {
                livingEntity.setDespawnCounter(0);
            }
        }
    }


    public Entity findRunner(World world, Vec3d position) {
        Box targetBox = new Box(
            position.subtract(MAX_DIAGONAL_DISTANCE_HALF, MAX_DIAGONAL_DISTANCE_HALF, MAX_DIAGONAL_DISTANCE_HALF),
            position.add(MAX_DIAGONAL_DISTANCE_HALF, MAX_DIAGONAL_DISTANCE_HALF, MAX_DIAGONAL_DISTANCE_HALF)
        );

        List<Entity> candidates = world.getOtherEntities(null, targetBox, x -> this.canBeRunnerAt(x, position));
        candidates.sort((a, b) -> (int)Math.round(a.squaredDistanceTo(position) - b.squaredDistanceTo(position)));
        return candidates.isEmpty() ? null : candidates.get(0);
    }

    public boolean canBeRunnerAt(Entity entity, Vec3d pos) {
        return this.isValidRunner(entity) && entity.squaredDistanceTo(pos) < MAX_SQUARED_DISTANCE;
    }

    public boolean isValidRunner(Entity entity) {
        if (entity == null || !entity.isAlive() || !ENERGY_MAP.containsKey(entity.getClass())) {
            return false;
        }

        boolean isValid = true;
        if (entity instanceof LivingEntity livingEntity) {
            isValid = !livingEntity.isBaby() && !livingEntity.isSpectator();
            isValid &= !livingEntity.isSneaking() && !livingEntity.isSwimming();
        }

        if (entity instanceof TameableEntity tameableEntity) {
            isValid &= !tameableEntity.isSitting();
        }

        return isValid;
    }

    public double getEnergy() {
        if (this.runner == null) {
            return 0;
        }

        float lf = ENERGY_MAP.getOrDefault(this.runner.getClass(), 0F);
        return lf + lf * 0.5F * this.runningTime / MAX_RUNNING_TIME;
    }

    public double extract(double maxAmount) {
        return Math.min(maxAmount, this.getEnergy());
    }

    private static ImmutableMap<Class<? extends Entity>, Float> createEnergyMap() {
        return ImmutableMap.<Class<? extends Entity>, Float>builder()
            .put(ChickenEntity.class, 1.6F)
            .put(PigEntity.class, 16F)
            .put(ServerPlayerEntity.class, 20F)
            .put(WolfEntity.class, 24F)
            .put(CreeperEntity.class, 80F)
            .put(EndermanEntity.class, 160F)
            .build();
    }
}