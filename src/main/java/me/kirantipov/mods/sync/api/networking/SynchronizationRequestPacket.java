package me.kirantipov.mods.sync.api.networking;

import me.kirantipov.mods.sync.Sync;
import me.kirantipov.mods.sync.api.core.Shell;
import me.kirantipov.mods.sync.api.core.ShellState;
import me.kirantipov.mods.sync.block.entity.AbstractShellContainerBlockEntity;
import me.kirantipov.mods.sync.util.BlockPosUtil;
import me.kirantipov.mods.sync.util.WorldUtil;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.Chunk;

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
        if (this.shellUuid == null) {
            reject(player, responseSender);
            return;
        }

        Shell shell = (Shell)player;
        ShellState state = shell.getShellStateByUuid(this.shellUuid);
        if (!shell.canBeApplied(state) || state.getProgress() < ShellState.PROGRESS_DONE) {
            reject(player, responseSender);
            return;
        }

        boolean isDead = player.isDead();
        BlockPos currentPos = player.getBlockPos();
        ServerWorld currentWorld = player.getServerWorld();
        Chunk currentChunk = currentWorld.getChunk(currentPos);
        BlockEntity currentShellContainerBE = isDead ? null : currentChunk.getBlockEntity(currentPos);
        Direction currentFacing = BlockPosUtil.getHorizontalFacing(currentPos, currentChunk).orElse(Direction.fromRotation(player.getYaw()).getOpposite());
        if (!isDead && (!(currentShellContainerBE instanceof AbstractShellContainerBlockEntity currentShellContainer) || currentShellContainer.getShell() != null)) {
            reject(player, responseSender);
            return;
        }

        Identifier targetWorldId = state.getWorld();
        ServerWorld targetWorld = WorldUtil.findWorld(server.getWorlds(), targetWorldId).orElse(null);
        if (targetWorld == null) {
            reject(player, responseSender);
            return;
        }

        BlockPos targetPos = state.getPos();
        Chunk targetChunk = targetWorld.getChunk(targetPos);
        BlockEntity targetShellContainerBE = targetChunk == null ? null : targetChunk.getBlockEntity(targetPos);
        if (!(targetShellContainerBE instanceof AbstractShellContainerBlockEntity targetShellContainer)) {
            reject(player, responseSender);
            return;
        }
        Direction targetFacing = BlockPosUtil.getHorizontalFacing(targetPos, targetChunk).orElse(Direction.NORTH);

        state = targetShellContainer.getShell();
        if (!shell.canBeApplied(state)) {
            reject(player, responseSender);
            return;
        }

        if (currentShellContainerBE instanceof AbstractShellContainerBlockEntity currentShellContainer) {
            ShellState storedState = ShellState.of(player, currentPos, currentShellContainer.getColor());
            currentShellContainer.setShell(storedState);
            shell.add(storedState);
        }

        targetShellContainer.setShell(null);
        shell.remove(state);
        shell.apply(state);

        new SynchronizationResponsePacket(currentWorld == targetWorld, targetWorldId, currentPos, currentFacing, targetPos, targetFacing).send(responseSender);
    }

    private static void reject(ServerPlayerEntity player, PacketSender responseSender) {
        BlockPos pos = player.getBlockPos();
        Direction facing = Direction.fromRotation(player.getYaw());
        new SynchronizationResponsePacket(true, WorldUtil.getId(player.world), pos, facing, pos, facing).send(responseSender);
    }
}