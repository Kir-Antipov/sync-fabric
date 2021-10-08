package me.kirantipov.mods.sync.api.networking;

import me.kirantipov.mods.sync.Sync;
import me.kirantipov.mods.sync.api.core.ShellState;
import me.kirantipov.mods.sync.api.event.PlayerSyncEvents;
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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class SynchronizationResponsePacket implements ClientPlayerPacket {
    private boolean enableCamera;
    private Identifier worldId;
    private BlockPos start;
    private Direction startFacing;
    private BlockPos target;
    private Direction targetFacing;
    private ShellState storedState;

    public SynchronizationResponsePacket(boolean enableCamera, Identifier worldId, BlockPos start, Direction startFacing, BlockPos target, Direction targetFacing, @Nullable ShellState storedState) {
        this.enableCamera = enableCamera;
        this.worldId = worldId;
        this.start = start;
        this.startFacing = startFacing;
        this.target = target;
        this.targetFacing = targetFacing;
        this.storedState = storedState;
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
        if (this.storedState == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeNbt(this.storedState.writeNbt(new NbtCompound()));
        }
    }

    @Override
    public void read(PacketByteBuf buffer) {
        this.enableCamera = buffer.readBoolean();
        this.worldId = buffer.readIdentifier();
        this.start = buffer.readBlockPos();
        this.startFacing = Direction.byId(buffer.readVarInt());
        this.target = buffer.readBlockPos();
        this.targetFacing = Direction.byId(buffer.readVarInt());
        this.storedState = buffer.readBoolean() ? ShellState.fromNbt(buffer.readUnlimitedNbt()) : null;
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

        if (this.enableCamera) {
            PersistentCameraEntityGoal cameraGoal = PersistentCameraEntityGoal.highwayToHell(this.start, this.startFacing, this.target, this.targetFacing, x -> restorePlayerState(client, player));
            PersistentCameraEntity.setup(client, cameraGoal);
        } else {
            restorePlayerState(client, player);
        }
    }

    @Environment(EnvType.CLIENT)
    private void restorePlayerState(MinecraftClient client, ClientPlayerEntity player) {
        PersistentCameraEntity.unset(client);
        HudController.restore();
        DeathScreenController.restore();

        boolean syncFailed = this.start.equals(this.target);
        if (!syncFailed) {
            PlayerSyncEvents.STOP_SYNCING.invoker().onStopSyncing(player, this.start, this.storedState);
        }
    }
}