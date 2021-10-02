package me.kirantipov.mods.sync.entity;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.authlib.GameProfile;
import me.kirantipov.mods.sync.api.core.ShellState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Environment(EnvType.CLIENT)
public class ShellEntity extends AbstractClientPlayerEntity {
    private static final Cache<UUID, PlayerListEntry> PLAYER_ENTRY_CACHE;

    public boolean isActive;
    public float pitchProgress;
    private final ShellState state;
    private Runnable onInitialized;
    private final PlayerListEntry playerEntry;

    public ShellEntity(ShellState state) {
        this(MinecraftClient.getInstance().world, state);
    }

    public ShellEntity(ClientWorld world, ShellState state) {
        super(world, getPlayerEntry(state).getProfile());
        this.isActive = false;
        this.pitchProgress = 0;
        this.state = state;
        state.getInventory().copyTo(this.getInventory());
        this.playerEntry = getPlayerEntry(state);
        this.refreshPositionAndAngles(state.getPos(), 0, 0);

        if (this.onInitialized != null) {
            this.onInitialized.run();
            this.onInitialized = null;
        }
    }

    public void onInitialized(Runnable runnable) {
        if (this.state == null) {
            this.onInitialized = runnable;
        } else if (runnable != null) {
            runnable.run();
        }
    }

    public ShellState getState() {
        return this.state;
    }

    @Override
    protected void dropInventory() {
        if (this.world instanceof ServerWorld serverWorld) {
            this.state.dropInventory(serverWorld, this.getBlockPos());
        }
    }

    @Override
    protected void dropXp() {
        if (this.world instanceof ServerWorld serverWorld) {
            this.state.dropXp(serverWorld, this.getBlockPos());
        }
    }

    @Override
    public boolean isCreative() {
        return true;
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean shouldRenderName() {
        return false;
    }

    @Override
    public boolean isPartVisible(PlayerModelPart modelPart) {
        return true;
    }

    @Override
    protected PlayerListEntry getPlayerListEntry() {
        return this.playerEntry;
    }

    private static PlayerListEntry getPlayerEntry(ShellState state) {
        PlayerListEntry entry = PLAYER_ENTRY_CACHE.getIfPresent(state.getOwnerUuid());
        if (entry == null) {
            ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
            if (networkHandler != null) {
                entry = networkHandler.getPlayerListEntry(state.getOwnerUuid());
                if (entry == null) {
                    entry = networkHandler.getPlayerListEntry(state.getOwnerName());
                }
            }

            if (entry == null) {
                entry = new PlayerListEntry(new PlayerListS2CPacket.Entry(new GameProfile(state.getOwnerUuid(), state.getOwnerName()), 0, null, null));
            }

            PLAYER_ENTRY_CACHE.put(state.getOwnerUuid(), entry);
        }
        return entry;
    }

    static {
        PLAYER_ENTRY_CACHE = CacheBuilder.newBuilder()
            .initialCapacity(20)
            .maximumSize(40)
            .expireAfterAccess(20, TimeUnit.MINUTES)
            .build();
    }
}