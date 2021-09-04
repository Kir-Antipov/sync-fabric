package me.kirantipov.mods.sync.mixin;

import com.mojang.authlib.GameProfile;
import me.kirantipov.mods.sync.api.core.Shell;
import me.kirantipov.mods.sync.api.core.ShellState;
import me.kirantipov.mods.sync.api.networking.SynchronizationRequestPacket;
import me.kirantipov.mods.sync.client.gui.controller.DeathScreenController;
import me.kirantipov.mods.sync.client.gui.controller.HudController;
import me.kirantipov.mods.sync.entity.PersistentCameraEntity;
import me.kirantipov.mods.sync.entity.PersistentCameraEntityGoal;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity implements Shell {
    @Final
    @Shadow
    protected MinecraftClient client;

    @Unique
    private boolean isArtificial = false;

    @Unique
    private ConcurrentMap<UUID, ShellState> shellsById = new ConcurrentHashMap<>();


    private MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
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
    public void apply(ShellState state) {
        throw new IllegalStateException("We're not supposed to do this");
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
        return this.shellsById.get(uuid);
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
        ShellState respawnShell = canRespawn ? this.shellsById.values().stream().filter(x -> this.canBeApplied(x) && x.getProgress() >= ShellState.PROGRESS_DONE).sorted((a, b) -> Boolean.compare(a.isArtificial(), b.isArtificial())).findAny().orElse(null) : null;
        if (respawnShell == null) {
            return;
        }

        Direction facing = Direction.fromRotation(this.getYaw()).getOpposite();
        PersistentCameraEntityGoal cameraGoal = PersistentCameraEntityGoal.limbo(this.getBlockPos(), facing, respawnShell.getPos(), x -> new SynchronizationRequestPacket(respawnShell).send());

        HudController.hide();
        DeathScreenController.suspend();
        PersistentCameraEntity.setup(this.client, cameraGoal);
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