package me.kirantipov.mods.sync.api.networking;

import me.kirantipov.mods.sync.Sync;
import me.kirantipov.mods.sync.api.core.Shell;
import me.kirantipov.mods.sync.api.core.ShellState;
import me.kirantipov.mods.sync.api.core.ShellStateContainer;
import me.kirantipov.mods.sync.api.event.PlayerSyncEvents;
import me.kirantipov.mods.sync.util.BlockPosUtil;
import me.kirantipov.mods.sync.util.WorldUtil;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
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
            reject(PlayerSyncEvents.SyncFailureReason.INVALID_SHELL, player, Direction.fromRotation(player.getYaw()), responseSender);
            return;
        }

        BlockPos currentPos = player.getBlockPos();
        ServerWorld currentWorld = player.getServerWorld();
        Chunk currentChunk = currentWorld.getChunk(currentPos);
        Direction currentFacing = BlockPosUtil.getHorizontalFacing(currentPos, currentChunk).orElse(Direction.fromRotation(player.getYaw()).getOpposite());

        Shell shell = (Shell)player;
        ShellState state = shell.getShellStateByUuid(this.shellUuid);
        if (!shell.canBeApplied(state) || state.getProgress() < ShellState.PROGRESS_DONE) {
            reject(PlayerSyncEvents.SyncFailureReason.INVALID_SHELL, player, currentFacing, responseSender);
            return;
        }

        boolean isDead = player.isDead();
        ShellStateContainer currentShellContainer = isDead ? null : ShellStateContainer.find(currentWorld, currentPos);
        if (!isDead && (currentShellContainer == null || currentShellContainer.getShellState() != null)) {
            reject(PlayerSyncEvents.SyncFailureReason.INVALID_CURRENT_LOCATION, player, currentFacing, responseSender);
            return;
        }

        PlayerSyncEvents.ShellSelectionFailureReason selectionFailureReason = PlayerSyncEvents.ALLOW_SHELL_SELECTION.invoker().allowShellSelection(player, currentShellContainer);
        if (selectionFailureReason != null) {
            reject(selectionFailureReason::toText, player, currentFacing, responseSender);
            return;
        }

        Identifier targetWorldId = state.getWorld();
        ServerWorld targetWorld = WorldUtil.findWorld(server.getWorlds(), targetWorldId).orElse(null);
        if (targetWorld == null) {
            reject(PlayerSyncEvents.SyncFailureReason.INVALID_TARGET_LOCATION, player, currentFacing, responseSender);
            return;
        }

        BlockPos targetPos = state.getPos();
        Chunk targetChunk = targetWorld.getChunk(targetPos);
        ShellStateContainer targetShellContainer = targetChunk == null ? null : ShellStateContainer.find(targetWorld, state);
        if (targetShellContainer == null) {
            reject(PlayerSyncEvents.SyncFailureReason.INVALID_TARGET_LOCATION, player, currentFacing, responseSender);
            return;
        }
        Direction targetFacing = BlockPosUtil.getHorizontalFacing(targetPos, targetChunk).orElse(Direction.NORTH);

        state = targetShellContainer.getShellState();
        PlayerSyncEvents.SyncFailureReason finalFailureReason = shell.canBeApplied(state) ? PlayerSyncEvents.ALLOW_SYNCING.invoker().allowSync(player, state) : PlayerSyncEvents.SyncFailureReason.INVALID_SHELL;
        if (finalFailureReason != null) {
            reject(finalFailureReason, player, currentFacing, responseSender);
            return;
        }

        PlayerSyncEvents.START_SYNCING.invoker().onStartSyncing(player, state);

        ShellState storedState = null;
        if (currentShellContainer != null) {
            storedState = ShellState.of(player, currentPos, currentShellContainer.getColor());
            currentShellContainer.setShellState(storedState);
            if (currentShellContainer.isRemotelyAccessible()) {
                shell.add(storedState);
            }
        }

        targetShellContainer.setShellState(null);
        shell.remove(state);
        shell.apply(state);

        new SynchronizationResponsePacket(currentWorld == targetWorld, targetWorldId, currentPos, currentFacing, targetPos, targetFacing, storedState).send(responseSender);

        PlayerSyncEvents.STOP_SYNCING.invoker().onStopSyncing(player, currentPos, storedState);
    }

    private static void reject(PlayerSyncEvents.SyncFailureReason reason, ServerPlayerEntity player, Direction facing, PacketSender responseSender) {
        player.sendMessage(reason.toText(), false);
        BlockPos pos = player.getBlockPos();
        new SynchronizationResponsePacket(true, WorldUtil.getId(player.world), pos, facing, pos, facing, null).send(responseSender);
    }
}