package me.kirantipov.mods.sync.api.networking;

import me.kirantipov.mods.sync.Sync;
import me.kirantipov.mods.sync.client.gui.controller.DeathScreenController;
import me.kirantipov.mods.sync.client.gui.controller.HudController;
import me.kirantipov.mods.sync.entity.PersistentCameraEntity;
import me.kirantipov.mods.sync.entity.PersistentCameraEntityGoal;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class SynchronizationResponsePacket implements ClientPlayerPacket {
    private boolean enableCamera;
    private Identifier worldId;
    private BlockPos start;
    private Direction startFacing;
    private BlockPos target;
    private Direction targetFacing;

    public SynchronizationResponsePacket(boolean enableCamera, Identifier worldId, BlockPos start, Direction startFacing, BlockPos target, Direction targetFacing) {
        this.enableCamera = enableCamera;
        this.worldId = worldId;
        this.start = start;
        this.startFacing = startFacing;
        this.target = target;
        this.targetFacing = targetFacing;
    }

    @Override
    public Identifier getId() {
        return Sync.locate("packet.shell.synchronization.response");
    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeBoolean(this.enableCamera);
        buffer.writeIdentifier(this.worldId);
        buffer.writeBlockPos(this.start);
        buffer.writeVarInt(this.startFacing.getId());
        buffer.writeBlockPos(this.target);
        buffer.writeVarInt(this.targetFacing.getId());
    }

    @Override
    public void read(PacketByteBuf buffer) {
        this.enableCamera = buffer.readBoolean();
        this.worldId = buffer.readIdentifier();
        this.start = buffer.readBlockPos();
        this.startFacing = Direction.byId(buffer.readVarInt());
        this.target = buffer.readBlockPos();
        this.targetFacing = Direction.byId(buffer.readVarInt());
    }

    @Override
    public Identifier getTargetWorldId() {
        return this.worldId;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void execute(MinecraftClient client, ClientPlayerEntity player, ClientPlayNetworkHandler handler, PacketSender responseSender) {
        float yaw = this.targetFacing.getOpposite().asRotation();
        player.setYaw(yaw);
        if (player.getHealth() <= 0) {
            player.setHealth(0.01F);
        }
        player.deathTime = 0;
        player.prevYaw = yaw;
        player.lastRenderYaw = player.renderYaw = yaw;
        player.prevBodyYaw = player.bodyYaw = yaw;
        player.prevHeadYaw = player.headYaw = yaw;

        player.setPitch(0);
        player.lastRenderPitch = player.renderPitch = 0;
        player.prevPitch = 0;

        if (!this.enableCamera) {
            restorePlayerState(client);
            return;
        }

        PersistentCameraEntityGoal cameraGoal = PersistentCameraEntityGoal.highwayToHell(this.start, this.startFacing, this.target, this.targetFacing, x -> restorePlayerState(client));
        PersistentCameraEntity.setup(client, cameraGoal);
    }

    @Environment(EnvType.CLIENT)
    private static void restorePlayerState(MinecraftClient client) {
        PersistentCameraEntity.unset(client);
        HudController.restore();
        DeathScreenController.restore();
    }
}