package dev.kir.sync.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Date;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class PersistentCameraEntity extends ClientPlayerEntity {
    private long lastMovementTime;
    private float initialYaw;
    private float initialPitch;
    private double initialDistance;
    private PersistentCameraEntityGoal goal;

    private PersistentCameraEntity(MinecraftClient client) {
        super(client, Objects.requireNonNull(client.player).clientWorld, client.player.networkHandler, client.player.getStatHandler(), client.player.getRecipeBook(), false, false);
        ClientPlayerEntity player = client.player;
        this.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
        this.setRotation(player.getYaw(), player.getPitch());
        this.updateLastTickValues();
        this.noClip = true;
    }

    @Override
    public boolean isSpectator() {
        return true;
    }

    @Override
    public void tickMovement() {
        PersistentCameraEntityGoal goal = this.goal;
        long currentTime = new Date().getTime();
        if (goal == null || this.lastMovementTime < 0) {
            this.lastMovementTime = currentTime;
            return;
        }
        if (this.lastMovementTime > currentTime) {
            return;
        }
        this.updateLastTickValues();

        Vec3d currentPos = this.getPos();
        Vec3d currentVelocity = this.getVelocity();
        Vec3d newPos = currentPos.add(currentVelocity.multiply(currentTime - this.lastMovementTime));
        Vec3d currentDiff = goal.pos.subtract(currentPos);
        Vec3d newDiff = goal.pos.subtract(newPos);

        if (Math.signum(currentDiff.x) != Math.signum(newDiff.x)) {
            newPos = new Vec3d(goal.pos.x, newPos.y, newPos.z);
            currentVelocity = new Vec3d(0, currentVelocity.y, currentVelocity.z);
        }
        if (Math.signum(currentDiff.y) != Math.signum(newDiff.y)) {
            newPos = new Vec3d(newPos.x, goal.pos.y, newPos.z);
            currentVelocity = new Vec3d(currentVelocity.x, 0, currentVelocity.z);
        }
        if (Math.signum(currentDiff.z) != Math.signum(newDiff.z)) {
            newPos = new Vec3d(newPos.x, newPos.y, goal.pos.z);
            currentVelocity = new Vec3d(currentVelocity.x, currentVelocity.y, 0);
        }

        this.setPosition(newPos);
        this.setVelocity(currentVelocity);

        float factor = 1F - (float)(goal.pos.distanceTo(newPos) / this.initialDistance);
        float newYaw = this.initialYaw + (goal.yaw - this.initialYaw) * factor;
        float newPitch = this.initialPitch + (goal.pitch - this.initialPitch) * factor;
        this.refreshPositionAndAngles(newPos.x, newPos.y, newPos.z, newYaw, newPitch);
        this.setRotation(newYaw, newPitch);
        this.setYaw(newYaw);
        this.setHeadYaw(newYaw);
        this.setBodyYaw(newYaw);
        this.renderYaw = this.renderYaw + (newYaw - this.renderYaw) * 0.5F;
        this.renderPitch = this.renderPitch + (newPitch - this.renderPitch) * 0.5F;

        this.lastMovementTime = currentTime;
        if (this.getPos().equals(goal.pos)) {
            this.updateLastTickValues();
            this.setGoal(null);
            goal.finish(this);
        }
    }

    private void updateLastTickValues()
    {
        this.lastRenderX = this.getX();
        this.lastRenderY = this.getY();
        this.lastRenderZ = this.getZ();

        this.prevX = this.getX();
        this.prevY = this.getY();
        this.prevZ = this.getZ();

        this.lastRenderYaw = this.renderYaw;
        this.lastRenderPitch = this.renderPitch;

        this.prevHeadYaw = this.headYaw;
    }

    public void setGoal(PersistentCameraEntityGoal goal) {
        this.goal = goal;
        this.initialYaw = this.getYaw();
        this.initialPitch = this.getPitch();
        this.lastMovementTime = -1;

        double dX = 0;
        double dY = 0;
        double dZ = 0;
        double duration = 0;
        if (goal != null) {
            Vec3d pos = this.getPos();
            dX = goal.pos.x - pos.x;
            dY = goal.pos.y - pos.y;
            dZ = goal.pos.z - pos.z;
            duration = goal.duration;
            if (goal.delay > 0) {
                this.lastMovementTime = new Date().getTime() + goal.delay;
            }
        }

        this.initialDistance = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
        this.setVelocity(new Vec3d(dX, dY, dZ).multiply(1.0 / Math.max(1, duration)));
    }

    public PersistentCameraEntityGoal getGoal() {
        return this.goal;
    }

    public static void setup(MinecraftClient client, PersistentCameraEntityGoal goal) {
        AbstractClientPlayerEntity player = client.player;
        if (player == null || player.clientWorld == null) {
            return;
        }

        if (!(client.getCameraEntity() instanceof PersistentCameraEntity)) {
            client.setCameraEntity(new PersistentCameraEntity(client));
        }

        PersistentCameraEntity camera = (PersistentCameraEntity)Objects.requireNonNull(client.getCameraEntity());
        camera.setGoal(goal);
    }

    public static void unset(MinecraftClient client) {
        if (client.getCameraEntity() instanceof PersistentCameraEntity camera) {
            camera.setGoal(null);
            client.setCameraEntity(client.player);
        }
    }

    private static void onTick(MinecraftClient client) {
        if (!(client.getCameraEntity() instanceof PersistentCameraEntity camera) || camera.goal == null) {
            return;
        }

        camera.tickMovement();
    }

    static {
        ClientTickEvents.START_CLIENT_TICK.register(PersistentCameraEntity::onTick);
    }
}