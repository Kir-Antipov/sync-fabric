package dev.kir.sync.mixin;

import com.mojang.authlib.GameProfile;
import dev.kir.sync.Sync;
import dev.kir.sync.api.event.PlayerSyncEvents;
import dev.kir.sync.api.networking.SynchronizationRequestPacket;
import dev.kir.sync.api.shell.ClientShell;
import dev.kir.sync.api.shell.ShellState;
import dev.kir.sync.client.gui.controller.DeathScreenController;
import dev.kir.sync.client.gui.controller.HudController;
import dev.kir.sync.api.shell.ShellPriority;
import dev.kir.sync.util.BlockPosUtil;
import dev.kir.sync.entity.PersistentCameraEntity;
import dev.kir.sync.entity.PersistentCameraEntityGoal;
import dev.kir.sync.util.WorldUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity implements ClientShell {
    @Final
    @Shadow
    protected MinecraftClient client;

    @Unique
    private boolean isArtificial = false;

    @Unique
    private ConcurrentMap<UUID, ShellState> shellsById = new ConcurrentHashMap<>();


    private ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }


    @Override
    public @Nullable PlayerSyncEvents.SyncFailureReason beginSync(ShellState state) {
        ClientWorld world = this.clientWorld;
        if (world == null) {
            return PlayerSyncEvents.SyncFailureReason.OTHER_PROBLEM;
        }

        PlayerSyncEvents.SyncFailureReason failureReason =
                this.canBeApplied(state) && state.getProgress() >= ShellState.PROGRESS_DONE
                        ? PlayerSyncEvents.ALLOW_SYNCING.invoker().allowSync(this, state)
                        : PlayerSyncEvents.SyncFailureReason.INVALID_SHELL;

        if (failureReason != null) {
            return failureReason;
        }

        PlayerSyncEvents.START_SYNCING.invoker().onStartSyncing(this, state);

        BlockPos pos = this.getBlockPos();
        Direction facing = BlockPosUtil.getHorizontalFacing(pos, world).orElse(this.getHorizontalFacing().getOpposite());
        SynchronizationRequestPacket request = new SynchronizationRequestPacket(state);
        PersistentCameraEntityGoal cameraGoal = this.isDead()
                ? PersistentCameraEntityGoal.limbo(pos, facing, state.getPos(), __ -> request.send())
                : PersistentCameraEntityGoal.stairwayToHeaven(pos, facing, state.getPos(), __ -> request.send());

        HudController.hide();
        if (this.isDead()) {
            DeathScreenController.suspend();
        }
        this.client.setScreen(null);
        PersistentCameraEntity.setup(this.client, cameraGoal);
        return null;
    }

    @Override
    public void endSync(Identifier startWorld, BlockPos startPos, Direction startFacing, Identifier targetWorld, BlockPos targetPos, Direction targetFacing, @Nullable ShellState storedState) {
        ClientPlayerEntity player = (ClientPlayerEntity)(Object)this;

        if (this.getHealth() <= 0) {
            this.setHealth(0.01F);
        }
        this.deathTime = 0;

        float yaw = targetFacing.getOpposite().asRotation();
        this.setYaw(yaw);
        this.prevYaw = yaw;
        this.prevBodyYaw = this.bodyYaw = yaw;
        this.prevHeadYaw = this.headYaw = yaw;
        player.lastRenderYaw = player.renderYaw = yaw;

        this.setPitch(0);
        this.prevPitch = 0;
        player.lastRenderPitch = player.renderPitch = 0;

        Runnable restore = () -> {
            PersistentCameraEntity.unset(this.client);
            HudController.restore();
            DeathScreenController.restore();

            boolean syncFailed = Objects.equals(startPos, targetPos);
            if (!syncFailed) {
                PlayerSyncEvents.STOP_SYNCING.invoker().onStopSyncing(this, startPos, storedState);
            }
        };

        boolean enableCamera = Objects.equals(startWorld, targetWorld);
        if (enableCamera) {
            PersistentCameraEntityGoal cameraGoal = PersistentCameraEntityGoal.highwayToHell(startPos, startFacing, targetPos, targetFacing, __ -> restore.run());
            PersistentCameraEntity.setup(this.client, cameraGoal);
        } else {
            restore.run();
        }
    }

    @Override
    public UUID getShellOwnerUuid() {
        return this.getGameProfile().getId();
    }

    @Override
    public boolean isArtificial() {
        return this.isArtificial;
    }

    @Override
    public void changeArtificialStatus(boolean isArtificial) {
        this.isArtificial = isArtificial;
    }

    @Override
    public void setAvailableShellStates(Stream<ShellState> states) {
        this.shellsById = states.collect(Collectors.toConcurrentMap(ShellState::getUuid, x -> x));
    }

    @Override
    public Stream<ShellState> getAvailableShellStates() {
        return this.shellsById.values().stream();
    }

    @Override
    public ShellState getShellStateByUuid(UUID uuid) {
        return uuid == null ? null : this.shellsById.get(uuid);
    }

    @Override
    public void add(ShellState state) {
        if (this.canBeApplied(state)) {
            this.shellsById.put(state.getUuid(), state);
        }
    }

    @Override
    public void remove(ShellState state) {
        if (state != null) {
            this.shellsById.remove(state.getUuid());
        }
    }

    @Override
    public void update(ShellState state) {
        if (this.canBeApplied(state) || state != null && this.shellsById.containsKey(state.getUuid())) {
            this.shellsById.put(state.getUuid(), state);
        }
    }

    @Override
    public void changeLookDirection(double cursorDeltaX, double cursorDeltaY) {
        if (this.client.getCameraEntity() == this) {
            super.changeLookDirection(cursorDeltaX, cursorDeltaY);
        }
    }

    @Override
    public void setHealth(float health) {
        super.setHealth(health);
        if (health <= 0F) {
            this.onDeath();
        }
    }

    @Unique
    private void onDeath() {
        boolean canRespawn = this.isArtificial() && this.shellsById.size() != 0;
        BlockPos pos = this.getBlockPos();
        Identifier world = WorldUtil.getId(this.world);
        Comparator<ShellState> comparator = ShellPriority.asComparator(world, pos, Sync.getConfig().syncPriority.stream().map(x -> x.priority));
        ShellState respawnShell = canRespawn ? this.shellsById.values().stream().filter(x -> this.canBeApplied(x) && x.getProgress() >= ShellState.PROGRESS_DONE).min(comparator).orElse(null) : null;
        if (respawnShell != null) {
            this.beginSync(respawnShell);
        }
    }

    @Inject(method = "updatePostDeath", at = @At("HEAD"), cancellable = true)
    private void updatePostDeath(CallbackInfo ci) {
        if (this.client.currentScreen instanceof DeathScreen) {
            this.deathTime = MathHelper.clamp(this.deathTime, 0, 19);
        } else {
            this.deathTime = MathHelper.clamp(++this.deathTime, 0, 20);
            ci.cancel();
        }
    }
}