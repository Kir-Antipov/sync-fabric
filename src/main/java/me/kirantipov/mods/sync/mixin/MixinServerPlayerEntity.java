package me.kirantipov.mods.sync.mixin;

import com.mojang.authlib.GameProfile;
import me.kirantipov.mods.sync.api.core.Shell;
import me.kirantipov.mods.sync.api.core.ShellState;
import me.kirantipov.mods.sync.api.core.ShellStateManager;
import me.kirantipov.mods.sync.api.core.ShellStateUpdateType;
import me.kirantipov.mods.sync.api.networking.PlayerIsAlivePacket;
import me.kirantipov.mods.sync.api.networking.ShellStateUpdatePacket;
import me.kirantipov.mods.sync.api.networking.ShellUpdatePacket;
import me.kirantipov.mods.sync.util.BlockPosUtil;
import me.kirantipov.mods.sync.util.WorldUtil;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
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

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity implements Shell {
    @Shadow
    private int syncedExperience;

    @Shadow
    private float syncedHealth;

    @Shadow
    private int syncedFoodLevel;

    @Final
    @Shadow
    public MinecraftServer server;

    @Unique
    private boolean isArtificial = false;

    @Unique
    private boolean shellDirty = false;

    @Unique
    private boolean undead = false;

    @Unique
    private ConcurrentMap<UUID, ShellState> shellsById = new ConcurrentHashMap<>();

    @Unique
    private Map<UUID, Pair<ShellStateUpdateType, ShellState>> shellStateChanges = new ConcurrentHashMap<>();


    private MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
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
        if (this.isArtificial != isArtificial) {
            this.isArtificial = isArtificial;
            this.shellDirty = true;
        }
    }

    @Override
    public void apply(ShellState state) {
        if (!this.canBeApplied(state) || state.getProgress() < ShellState.PROGRESS_DONE) {
            return;
        }

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity)(Object)this;
        MinecraftServer server = Objects.requireNonNull(this.world.getServer());
        ServerWorld targetWorld = WorldUtil.findWorld(server.getWorlds(), state.getWorld()).orElse(null);
        if (targetWorld == null) {
            return;
        }

        this.dropShoulderEntities();
        this.extinguish();
        this.setFrozenTicks(0);
        this.setOnFire(false);
        this.clearStatusEffects();

        new PlayerIsAlivePacket(serverPlayer).sendToAll(server);
        this.teleport(targetWorld, state.getPos());
        this.isArtificial = state.isArtificial();

        PlayerInventory inventory = this.getInventory();
        int selectedSlot = inventory.selectedSlot;
        inventory.clone(state.getInventory());
        inventory.selectedSlot = selectedSlot;

        serverPlayer.changeGameMode(GameMode.byId(state.getGameMode()));
        this.setHealth(state.getHealth());
        this.experienceLevel = state.getExperienceLevel();
        this.experienceProgress = state.getExperienceProgress();
        this.totalExperience = state.getTotalExperience();
        this.getHungerManager().setFoodLevel(state.getFoodLevel());
        this.getHungerManager().setSaturationLevel(state.getSaturationLevel());
        this.getHungerManager().setExhaustion(state.getExhaustion());

        this.undead = false;
        this.dead = false;
        this.deathTime = 0;
        this.syncedExperience = -1;
        this.syncedHealth = -1;
        this.syncedFoodLevel = -1;
        this.shellDirty = true;
    }

    @Override
    public Stream<ShellState> getAvailableShellStates() {
        return this.shellsById.values().stream();
    }

    @Override
    public void setAvailableShellStates(Stream<ShellState> states) {
        this.shellsById = states.collect(Collectors.toConcurrentMap(ShellState::getUuid, x -> x));
        this.shellDirty = true;
    }

    @Override
    public ShellState getShellStateByUuid(UUID uuid) {
        return this.shellsById.get(uuid);
    }

    @Override
    public void add(ShellState state) {
        if (!this.canBeApplied(state) || this.shellsById.get(state.getUuid()) == state) {
            return;
        }

        this.shellsById.put(state.getUuid(), state);
        this.shellStateChanges.put(state.getUuid(), new Pair<>(ShellStateUpdateType.ADD, state));
    }

    @Override
    public void remove(ShellState state) {
        if (state == null) {
            return;
        }

        if (this.shellsById.remove(state.getUuid()) != null) {
            this.shellStateChanges.put(state.getUuid(), new Pair<>(ShellStateUpdateType.REMOVE, state));
        }
    }

    @Override
    public void update(ShellState state) {
        if (state == null) {
            return;
        }

        boolean updated;
        if (this.canBeApplied(state)) {
            updated = this.shellsById.put(state.getUuid(), state) != null;
        } else {
            updated = this.shellsById.computeIfPresent(state.getUuid(), (a, b) -> state) != null;
        }
        this.shellStateChanges.put(state.getUuid(), new Pair<>(updated ? ShellStateUpdateType.UPDATE : ShellStateUpdateType.ADD, state));
    }

    @Inject(method = "playerTick", at = @At("HEAD"))
    private void playerTick(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;

        if (this.shellDirty) {
            this.shellDirty = false;
            this.shellStateChanges.clear();
            new ShellUpdatePacket(WorldUtil.getId(this.world), this.isArtificial, this.shellsById.values()).send(player);
        }

        for (Pair<ShellStateUpdateType, ShellState> upd : this.shellStateChanges.values()) {
            new ShellStateUpdatePacket(upd.getLeft(), upd.getRight()).send(player);
        }
        this.shellStateChanges.clear();
    }

    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    private void onDeath(DamageSource source, CallbackInfo ci) {
        if (!this.isArtificial) {
            return;
        }

        ShellState respawnShell = this.shellsById.values().stream().filter(x -> this.canBeApplied(x) && x.getProgress() >= ShellState.PROGRESS_DONE).findAny().orElse(null);
        if (respawnShell == null) {
            return;
        }

        if (this.world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES)) {
            this.sendDeathMessageInChat();
        }

        if (this.world.getGameRules().getBoolean(GameRules.FORGIVE_DEAD_PLAYERS)) {
            this.forgiveMobAnger();
        }

        if (!this.isSpectator()) {
            this.drop(source);
        }

        this.undead = true;
        ci.cancel();
    }

    @Override
    protected void updatePostDeath() {
        this.deathTime = MathHelper.clamp(++this.deathTime, 0, 20);
        if (this.isArtificial && this.shellsById.values().stream().anyMatch(x -> this.canBeApplied(x) && x.getProgress() >= ShellState.PROGRESS_DONE)) {
            return;
        }

        if (this.undead) {
            this.onDeath(DamageSource.MAGIC);
            this.undead = false;
        }

        if (this.deathTime == 20) {
            this.world.sendEntityStatus(this, (byte)60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Unique
    private void sendDeathMessageInChat() {
        Text text = this.getDamageTracker().getDeathMessage();
        AbstractTeam team = this.getScoreboardTeam();
        if (team != null && team.getDeathMessageVisibilityRule() != AbstractTeam.VisibilityRule.ALWAYS) {
            if (team.getDeathMessageVisibilityRule() == AbstractTeam.VisibilityRule.HIDE_FOR_OTHER_TEAMS) {
                this.server.getPlayerManager().sendToTeam(this, text);
            } else if (team.getDeathMessageVisibilityRule() == AbstractTeam.VisibilityRule.HIDE_FOR_OWN_TEAM) {
                this.server.getPlayerManager().sendToOtherTeams(this, text);
            }
        } else {
            this.server.getPlayerManager().broadcastChatMessage(text, MessageType.SYSTEM, Util.NIL_UUID);
        }
    }

    @Shadow
    protected abstract void forgiveMobAnger();

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        NbtList shellList = new NbtList();
        this.shellsById
            .values()
            .stream()
            .map(x -> x.writeNbt(new NbtCompound()))
            .forEach(shellList::add);

        nbt.putBoolean("IsArtificial", this.isArtificial);
        nbt.put("Shells", shellList);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        this.isArtificial = nbt.getBoolean("IsArtificial");
        this.shellsById = nbt.getList("Shells", NbtElement.COMPOUND_TYPE)
            .stream()
            .map(x -> ShellState.fromNbt((NbtCompound)x))
            .collect(Collectors.toConcurrentMap(ShellState::getUuid, x -> x));

        Collection<Pair<ShellStateUpdateType, ShellState>> updates = ((ShellStateManager)this.server).popPendingUpdates(this.uuid);
        for (Pair<ShellStateUpdateType, ShellState> update : updates) {
            ShellState state = update.getRight();
            switch (update.getLeft()) {
                case ADD, UPDATE -> {
                    if (this.uuid.equals(state.getOwnerUuid())) {
                        this.shellsById.put(state.getUuid(), state);
                    }
                }
                case REMOVE -> this.shellsById.remove(state.getUuid());
            }
        }

        this.shellStateChanges = new HashMap<>();
        this.shellDirty = true;
    }

    @Inject(method = "copyFrom", at = @At("HEAD"))
    private void copyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        Shell shell = (Shell)oldPlayer;
        this.isArtificial = alive && shell.isArtificial();
        this.shellsById = shell.getAvailableShellStates().collect(Collectors.toConcurrentMap(ShellState::getUuid, x -> x));
        this.shellStateChanges = new HashMap<>();
        this.shellDirty = true;
    }

    @Inject(method = "setWorld", at = @At("HEAD"))
    private void setWorld(ServerWorld world, CallbackInfo ci) {
        if (world != this.world) {
            this.shellDirty = true;
        }
    }

    @Unique
    private void teleport(ServerWorld targetWorld, BlockPos pos) {
        Chunk chunk = targetWorld.getChunk(pos);
        double x = pos.getX() + 0.5;
        double y = pos.getY();
        double z = pos.getZ() + 0.5;
        float yaw = BlockPosUtil.getHorizontalFacing(pos, chunk).map(d -> d.getOpposite().asRotation()).orElse(0F);
        float pitch = 0;

        if (this.world == targetWorld) {
            this.setRotation(yaw, pitch);
            this.refreshPositionAfterTeleport(x, y, z);
            return;
        }

        ServerWorld serverWorld = (ServerWorld)this.world;
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity)(Object)this;

        WorldProperties worldProperties = targetWorld.getLevelProperties();
        serverPlayer.networkHandler.sendPacket(new PlayerRespawnS2CPacket(targetWorld.getDimension(), targetWorld.getRegistryKey(), BiomeAccess.hashSeed(targetWorld.getSeed()), serverPlayer.interactionManager.getGameMode(), serverPlayer.interactionManager.getPreviousGameMode(), targetWorld.isDebugWorld(), targetWorld.isFlat(), true));
        serverPlayer.networkHandler.sendPacket(new DifficultyS2CPacket(worldProperties.getDifficulty(), worldProperties.isDifficultyLocked()));
        PlayerManager playerManager = Objects.requireNonNull(this.world.getServer()).getPlayerManager();
        playerManager.sendCommandTree(serverPlayer);
        serverWorld.removePlayer(serverPlayer, RemovalReason.CHANGED_DIMENSION);
        this.unsetRemoved();
        serverPlayer.setWorld(targetWorld);
        targetWorld.onPlayerChangeDimension(serverPlayer);
        this.setRotation(yaw, pitch);
        this.refreshPositionAfterTeleport(x, y, z);
        serverPlayer.networkHandler.sendPacket(new PlayerAbilitiesS2CPacket(serverPlayer.getAbilities()));
        playerManager.sendWorldInfo(serverPlayer, targetWorld);
        playerManager.sendPlayerStatus(serverPlayer);
    }
}