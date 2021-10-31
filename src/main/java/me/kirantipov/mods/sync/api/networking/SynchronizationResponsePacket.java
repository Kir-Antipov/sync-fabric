package me.kirantipov.mods.sync.api.networking;

import me.kirantipov.mods.sync.Sync;
import me.kirantipov.mods.sync.api.core.ClientShell;
import me.kirantipov.mods.sync.api.core.ShellState;
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
    private Identifier startWorld;
    private BlockPos startPos;
    private Direction startFacing;
    private Identifier targetWorld;
    private BlockPos targetPos;
    private Direction targetFacing;
    private ShellState storedState;

    public SynchronizationResponsePacket(Identifier startWorld, BlockPos startPos, Direction startFacing, Identifier targetWorld, BlockPos targetPos, Direction targetFacing, @Nullable ShellState storedState) {
        this.startWorld = startWorld;
        this.startPos = startPos;
        this.startFacing = startFacing;
        this.targetWorld = targetWorld;
        this.targetPos = targetPos;
        this.targetFacing = targetFacing;
        this.storedState = storedState;
    }

    @Override
    public Identifier getId() {
        return Sync.locate("packet.shell.synchronization.response");
    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeIdentifier(this.startWorld);
        buffer.writeBlockPos(this.startPos);
        buffer.writeVarInt(this.startFacing.getId());
        buffer.writeIdentifier(this.targetWorld);
        buffer.writeBlockPos(this.targetPos);
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
        this.startWorld = buffer.readIdentifier();
        this.startPos = buffer.readBlockPos();
        this.startFacing = Direction.byId(buffer.readVarInt());
        this.targetWorld = buffer.readIdentifier();
        this.targetPos = buffer.readBlockPos();
        this.targetFacing = Direction.byId(buffer.readVarInt());
        this.storedState = buffer.readBoolean() ? ShellState.fromNbt(buffer.readUnlimitedNbt()) : null;
    }

    @Override
    public Identifier getTargetWorldId() {
        return this.targetWorld;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void execute(MinecraftClient client, ClientPlayerEntity player, ClientPlayNetworkHandler handler, PacketSender responseSender) {
        ((ClientShell)player).endSync(this.startWorld, this.startPos, this.startFacing, this.targetWorld, this.targetPos, this.targetFacing, this.storedState);
    }
}