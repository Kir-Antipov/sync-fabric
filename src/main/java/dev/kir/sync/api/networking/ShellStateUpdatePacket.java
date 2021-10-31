package dev.kir.sync.api.networking;

import dev.kir.sync.Sync;
import dev.kir.sync.api.shell.Shell;
import dev.kir.sync.api.shell.ShellState;
import dev.kir.sync.api.shell.ShellStateUpdateType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.UUID;

public class ShellStateUpdatePacket implements ClientPlayerPacket {
    private ShellStateUpdateType type;
    private ShellState shellState;
    private UUID uuid;
    private float progress;
    private DyeColor color;
    private BlockPos pos;

    public ShellStateUpdatePacket(ShellStateUpdateType type, ShellState shellState) {
        this.type = type;
        this.shellState = shellState;
    }

    @Override
    public Identifier getId() {
        return Sync.locate("packet.shell.state.update");
    }

    @Override
    public void write(PacketByteBuf buffer) {
        if (this.shellState == null && this.type != ShellStateUpdateType.NONE) {
            throw new IllegalStateException();
        }

        buffer.writeEnumConstant(type);
        switch (type) {
            case ADD:
                buffer.writeNbt(this.shellState.writeNbt(new NbtCompound()));
                break;

            case REMOVE:
                buffer.writeUuid(this.shellState.getUuid());
                break;

            case UPDATE:
                buffer.writeUuid(this.shellState.getUuid());
                buffer.writeVarInt((int)(this.shellState.getProgress() * 100));
                buffer.writeVarInt(this.shellState.getColor() == null ? Byte.MAX_VALUE : this.shellState.getColor().getId());
                buffer.writeBlockPos(this.shellState.getPos());
                break;

            default:
                break;
        }
    }

    @Override
    public void read(PacketByteBuf buffer) {
        this.type = buffer.readEnumConstant(ShellStateUpdateType.class);
        switch (this.type) {
            case ADD:
                this.shellState = ShellState.fromNbt(buffer.readUnlimitedNbt());
                break;

            case REMOVE:
                this.uuid = buffer.readUuid();
                break;

            case UPDATE:
                this.uuid = buffer.readUuid();
                this.progress = MathHelper.clamp(buffer.readVarInt() / 100F, 0F, 1F);
                int colorId = buffer.readVarInt();
                this.color = colorId < 0 || colorId > 15 ? null : DyeColor.byId(colorId);
                this.pos = buffer.readBlockPos();
                break;

            default:
                break;
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void execute(MinecraftClient client, ClientPlayerEntity player, ClientPlayNetworkHandler handler, PacketSender responseSender) {
        Shell shell = (Shell)player;
        if (shell == null) {
            return;
        }

        ShellState state;
        switch (this.type) {
            case ADD:
                shell.add(this.shellState);
                break;

            case REMOVE:
                state = shell.getShellStateByUuid(this.uuid);
                if (state != null) {
                    shell.remove(state);
                }
                break;

            case UPDATE:
                state = shell.getShellStateByUuid(this.uuid);
                if (state != null) {
                    state.setProgress(this.progress);
                    state.setColor(this.color);
                    state.setPos(this.pos);
                }
                break;

            default:
                break;
        }
    }
}