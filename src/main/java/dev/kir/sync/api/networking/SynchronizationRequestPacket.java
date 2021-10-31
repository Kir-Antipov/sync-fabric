package dev.kir.sync.api.networking;

import dev.kir.sync.Sync;
import dev.kir.sync.util.BlockPosUtil;
import dev.kir.sync.api.shell.ServerShell;
import dev.kir.sync.api.shell.ShellState;
import dev.kir.sync.util.WorldUtil;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Objects;
import java.util.UUID;

public class SynchronizationRequestPacket implements ServerPlayerPacket {
    private UUID shellUuid;

    public SynchronizationRequestPacket(ShellState shell) {
        this.shellUuid = shell == null ? null : shell.getUuid();
    }

    public SynchronizationRequestPacket(UUID shellUuid) {
        this.shellUuid = shellUuid;
    }

    @Override
    public Identifier getId() {
        return Sync.locate("packet.shell.synchronization.request");
    }

    @Override
    public void write(PacketByteBuf buffer) {
        if (this.shellUuid == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeUuid(this.shellUuid);
        }
    }

    @Override
    public void read(PacketByteBuf buffer) {
        this.shellUuid = buffer.readBoolean() ? buffer.readUuid() : null;
    }

    @Override
    public void execute(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketSender responseSender) {
        ServerShell shell = (ServerShell)player;
        ShellState state = shell.getShellStateByUuid(this.shellUuid);

        BlockPos currentPos = player.getBlockPos();
        ServerWorld currentWorld = player.getServerWorld();
        Identifier currentWorldId = WorldUtil.getId(currentWorld);
        Direction currentFacing = BlockPosUtil.getHorizontalFacing(currentPos, currentWorld).orElse(player.getHorizontalFacing().getOpposite());

        shell.sync(state).ifLeft(storedState -> {
            Objects.requireNonNull(state);
            Identifier targetWorldId = state.getWorld();
            BlockPos targetPos = state.getPos();
            Direction targetFacing = player.getHorizontalFacing().getOpposite();
            new SynchronizationResponsePacket(currentWorldId, currentPos, currentFacing, targetWorldId, targetPos, targetFacing, storedState).send(responseSender);
        }).ifRight(failureReason -> {
            player.sendMessage(failureReason.toText(), false);
            new SynchronizationResponsePacket(currentWorldId, currentPos, currentFacing, currentWorldId, currentPos, currentFacing, null).send(responseSender);
        });
    }
}