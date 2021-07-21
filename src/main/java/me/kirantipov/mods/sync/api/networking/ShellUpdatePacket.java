package me.kirantipov.mods.sync.api.networking;

import me.kirantipov.mods.sync.Sync;
import me.kirantipov.mods.sync.api.core.Shell;
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

import java.util.Collection;
import java.util.List;

public class ShellUpdatePacket implements ClientPlayerPacket {
    private Identifier worldId;
    private boolean isArtificial;
    private Collection<ShellState> states;

    public ShellUpdatePacket(Identifier worldId, boolean isArtificial, Collection<ShellState> states) {
        this.worldId = worldId;
        this.isArtificial = isArtificial;
        this.states = states == null ? List.of() : states;
    }

    @Override
    public Identifier getId() {
        return Sync.locate("packet.shell.update");
    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeIdentifier(this.worldId);
        buffer.writeBoolean(this.isArtificial);
        buffer.writeVarInt(this.states.size());
        this.states.forEach(x -> buffer.writeNbt(x.writeNbt(new NbtCompound())));
    }

    @Override
    public void read(PacketByteBuf buffer) {
        this.worldId = buffer.readIdentifier();
        this.isArtificial = buffer.readBoolean();
        this.states = buffer.readList(subBuffer -> ShellState.fromNbt(subBuffer.readUnlimitedNbt()));
    }

    @Override
    public Identifier getTargetWorldId() {
        return this.worldId;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void execute(MinecraftClient client, ClientPlayerEntity player, ClientPlayNetworkHandler handler, PacketSender responseSender) {
        Shell shell = (Shell)player;
        shell.changeArtificialStatus(this.isArtificial);
        shell.setAvailableShellStates(this.states.stream());
    }
}