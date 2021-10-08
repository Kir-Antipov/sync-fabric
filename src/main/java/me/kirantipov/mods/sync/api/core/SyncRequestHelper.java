package me.kirantipov.mods.sync.api.core;

import me.kirantipov.mods.sync.api.event.PlayerSyncEvents;
import me.kirantipov.mods.sync.api.networking.SynchronizationRequestPacket;
import me.kirantipov.mods.sync.client.gui.controller.DeathScreenController;
import me.kirantipov.mods.sync.client.gui.controller.HudController;
import me.kirantipov.mods.sync.entity.PersistentCameraEntity;
import me.kirantipov.mods.sync.entity.PersistentCameraEntityGoal;
import me.kirantipov.mods.sync.util.BlockPosUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class SyncRequestHelper {
    @Nullable
    public static PlayerSyncEvents.SyncFailureReason tryRequestSync(MinecraftClient client, ShellState targetShell) {
        ClientPlayerEntity player = client.player;
        ClientWorld world = player == null ? null : player.clientWorld;
        if (world == null) {
            return PlayerSyncEvents.SyncFailureReason.OTHER_PROBLEM;
        }

        PlayerSyncEvents.SyncFailureReason failureReason =
            ((Shell)player).canBeApplied(targetShell) && targetShell.getProgress() >= ShellState.PROGRESS_DONE
                ? PlayerSyncEvents.ALLOW_SYNCING.invoker().allowSync(player, targetShell)
                : PlayerSyncEvents.SyncFailureReason.INVALID_SHELL;

        if (failureReason != null) {
            return failureReason;
        }

        PlayerSyncEvents.START_SYNCING.invoker().onStartSyncing(player, targetShell);

        BlockPos pos = player.getBlockPos();
        Direction facing = BlockPosUtil.getHorizontalFacing(pos, world).orElseGet(() -> Direction.fromRotation(player.getYaw()).getOpposite());
        SynchronizationRequestPacket request = new SynchronizationRequestPacket(targetShell);
        PersistentCameraEntityGoal cameraGoal = player.isDead()
                ? PersistentCameraEntityGoal.limbo(pos, facing, targetShell.getPos(), __ -> request.send())
                : PersistentCameraEntityGoal.stairwayToHeaven(pos, facing, targetShell.getPos(), __ -> request.send());

        HudController.hide();
        if (player.isDead()) {
            DeathScreenController.suspend();
        }
        client.setScreen(null);
        PersistentCameraEntity.setup(client, cameraGoal);
        return null;
    }
}
