package dev.kir.sync.compat.origins;

import dev.kir.sync.api.shell.ShellStateComponent;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.networking.ModPackets;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.registry.ModComponents;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

class OriginsShellStateComponent extends ShellStateComponent {
    private final ServerPlayerEntity player;
    private boolean activated;
    private NbtCompound originComponentNbt;
    private NbtCompound powerHolderComponentNbt;

    public OriginsShellStateComponent() {
        this(null, false);
    }

    public OriginsShellStateComponent(ServerPlayerEntity player) {
        this(player, true);
    }

    private OriginsShellStateComponent(ServerPlayerEntity player, boolean activated) {
        this.player = player;
        this.activated = activated;
    }

    @Override
    public String getId() {
        return "origins";
    }

    public boolean isActivated() {
        return this.activated;
    }

    public NbtCompound getOriginComponentNbt() {
        NbtCompound nbt = this.originComponentNbt;
        if (this.player != null) {
            nbt = new NbtCompound();
            ModComponents.ORIGIN.get(this.player).writeToNbt(nbt);
        }
        return nbt == null ? new NbtCompound() : nbt;
    }

    public NbtCompound getPowerHolderComponentNbt() {
        NbtCompound nbt = this.powerHolderComponentNbt;
        if (this.player != null) {
            nbt = new NbtCompound();
            PowerHolderComponent.KEY.get(this.player).writeToNbt(nbt);
        }
        return nbt == null ? new NbtCompound() : nbt;
    }

    @Override
    public void clone(ShellStateComponent component) {
        OriginsShellStateComponent other = component.as(OriginsShellStateComponent.class);
        if (other == null) {
            return;
        }

        this.originComponentNbt = other.getOriginComponentNbt();
        this.powerHolderComponentNbt = other.getPowerHolderComponentNbt();
        this.activated = other.isActivated();
        if (this.player == null) {
            return;
        }

        OriginComponent originComponent = ModComponents.ORIGIN.get(this.player);
        if (this.activated) {
            originComponent.readFromNbt(this.originComponentNbt);
            PowerHolderComponent powerHolderComponent = PowerHolderComponent.KEY.get(this.player);
            powerHolderComponent.readFromNbt(this.powerHolderComponentNbt);
            originComponent.sync();
        } else {
            for (OriginLayer layer : OriginLayers.getLayers()) {
                if(layer.isEnabled()) {
                    originComponent.setOrigin(layer, Origin.EMPTY);
                }
            }
            originComponent.checkAutoChoosingLayers(this.player, false);
            originComponent.sync();
            PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
            data.writeBoolean(false);
            ServerPlayNetworking.send(this.player, ModPackets.OPEN_ORIGIN_SCREEN, data);
            this.activated = true;
        }
    }

    @Override
    protected void readComponentNbt(NbtCompound nbt) {
        this.originComponentNbt = nbt.contains("origins", NbtElement.COMPOUND_TYPE) ? nbt.getCompound("origins") : new NbtCompound();
        this.powerHolderComponentNbt = nbt.contains("powers", NbtElement.COMPOUND_TYPE) ? nbt.getCompound("powers") : new NbtCompound();
        this.activated = nbt.getBoolean("activated");
    }

    @Override
    protected NbtCompound writeComponentNbt(NbtCompound nbt) {
        nbt.put("origins", this.getOriginComponentNbt());
        nbt.put("powers", this.getPowerHolderComponentNbt());
        nbt.putBoolean("activated", this.isActivated());
        return nbt;
    }
}
