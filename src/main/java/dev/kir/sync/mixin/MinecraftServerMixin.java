package dev.kir.sync.mixin;

import dev.kir.sync.api.shell.Shell;
import dev.kir.sync.api.shell.ShellState;
import dev.kir.sync.util.nbt.OfflinePlayerNbtManager;
import dev.kir.sync.api.shell.ShellStateManager;
import dev.kir.sync.api.shell.ShellStateUpdateType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(MinecraftServer.class)
abstract class MinecraftServerMixin implements ShellStateManager {
    @Shadow
    private PlayerManager playerManager;

    @Unique
    private final ConcurrentMap<UUID, ConcurrentMap<UUID, Pair<ShellStateUpdateType, ShellState>>> pendingShellStates = new ConcurrentHashMap<>();

    @Override
    public void setAvailableShellStates(UUID owner, Stream<ShellState> states) {
        Shell shell = this.getShellById(owner);
        if (shell != null) {
            shell.setAvailableShellStates(states);
        }
    }

    @Override
    public Stream<ShellState> getAvailableShellStates(UUID owner) {
        Shell shell = this.getShellById(owner);
        return shell == null ? Stream.of() : shell.getAvailableShellStates();
    }

    @Override
    public ShellState getShellStateByUuid(UUID owner, UUID uuid) {
        Shell shell = this.getShellById(owner);
        return shell == null ? null : shell.getShellStateByUuid(uuid);
    }

    @Override
    public void add(ShellState state) {
        Shell shell = this.getShellByItsState(state);
        if (shell != null) {
            shell.add(state);
        }
    }

    @Override
    public void remove(ShellState state) {
        Shell shell = this.getShellByItsState(state);
        if (shell == null) {
            this.putPendingUpdate(state, ShellStateUpdateType.REMOVE);
        } else {
            shell.remove(state);
        }
    }

    @Override
    public void update(ShellState state) {
        Shell shell = this.getShellByItsState(state);
        if (shell == null) {
            this.putPendingUpdate(state, ShellStateUpdateType.UPDATE);
        } else {
            shell.update(state);
        }
    }

    @Override
    public Collection<Pair<ShellStateUpdateType, ShellState>> peekPendingUpdates(UUID owner) {
        Map<UUID, Pair<ShellStateUpdateType, ShellState>> shells = this.pendingShellStates.get(owner);
        if (shells == null) {
            return List.of();
        }
        return shells.values();
    }

    @Override
    public void clearPendingUpdates(UUID owner) {
        this.pendingShellStates.remove(owner);
    }

    @Inject(method = "shutdown", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;disconnectAllPlayers()V"))
    private void onShutdown(CallbackInfo ci) {
        for (Map.Entry<UUID, ConcurrentMap<UUID, Pair<ShellStateUpdateType, ShellState>>> entry : this.pendingShellStates.entrySet()) {
            UUID userId = entry.getKey();
            Collection<Pair<ShellStateUpdateType, ShellState>> updates = entry.getValue().values();
            if (updates.size() == 0) {
                continue;
            }

            OfflinePlayerNbtManager.editPlayerNbt((MinecraftServer)(Object)this, userId, nbt -> {
                Map<UUID, ShellState> shells = nbt
                        .getList("Shells", NbtElement.COMPOUND_TYPE)
                        .stream()
                        .map(x -> ShellState.fromNbt((NbtCompound)x))
                        .collect(Collectors.toMap(ShellState::getUuid, x -> x));

                for (Pair<ShellStateUpdateType, ShellState> update : updates) {
                    ShellState state = update.getRight();
                    switch (update.getLeft()) {
                        case ADD, UPDATE -> {
                            if (userId.equals(state.getOwnerUuid())) {
                                shells.put(state.getUuid(), state);
                            }
                        }
                        case REMOVE -> shells.remove(state.getUuid());
                    }
                }

                NbtList shellList = new NbtList();
                shells.values().stream().map(x -> x.writeNbt(new NbtCompound())).forEach(shellList::add);
                nbt.put("Shells", shellList);
            });
        }
        this.pendingShellStates.clear();
    }

    @Unique
    private void putPendingUpdate(ShellState state, ShellStateUpdateType updateType) {
        if (state == null || updateType == ShellStateUpdateType.NONE) {
            return;
        }

        ConcurrentMap<UUID, Pair<ShellStateUpdateType, ShellState>> updates = this.pendingShellStates.get(state.getOwnerUuid());
        if (updates == null) {
            updates = new ConcurrentHashMap<>();
            this.pendingShellStates.put(state.getOwnerUuid(), updates);
        }
        updates.put(state.getUuid(), new Pair<>(updateType, state));
    }

    @Unique
    private Shell getShellById(UUID id) {
        return this.isValidShellOwnerUuid(id) ? (Shell)this.playerManager.getPlayer(id) : null;
    }

    @Unique
    private Shell getShellByItsState(ShellState state) {
        return this.isValidShellState(state) ? (Shell)this.playerManager.getPlayer(state.getOwnerUuid()) : null;
    }
}