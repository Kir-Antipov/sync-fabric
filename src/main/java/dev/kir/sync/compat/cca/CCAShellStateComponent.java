package dev.kir.sync.compat.cca;

import dev.kir.sync.api.shell.ShellStateComponent;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public abstract class CCAShellStateComponent extends ShellStateComponent {
    private final ServerPlayerEntity player;
    private final ComponentKey<?> componentKey;
    private final Class<? extends CCAShellStateComponent> thisClass;
    private NbtCompound componentNbt;

    protected CCAShellStateComponent(@Nullable ServerPlayerEntity player, ComponentKey<?> componentKey) {
        this.player = player;
        this.componentKey = componentKey;
        this.thisClass = this.getClass();
    }

    public NbtCompound getComponentNbt() {
        NbtCompound nbt = this.componentNbt;
        if (this.player != null) {
            nbt = new NbtCompound();
            this.componentKey.get(this.player).writeToNbt(nbt);
        }
        return nbt == null ? new NbtCompound() : nbt;
    }

    @Override
    public String getId() {
        return this.componentKey.getId().getNamespace();
    }

    @Override
    public void clone(ShellStateComponent component) {
        CCAShellStateComponent other = component.as(this.thisClass);
        if (other == null) {
            return;
        }

        this.componentNbt = other.componentNbt;
        if (this.player == null) {
            return;
        }

        Component playerComponent = this.componentKey.get(this.player);
        playerComponent.readFromNbt(this.componentNbt);
        this.componentKey.sync(this.player);
    }

    @Override
    protected void readComponentNbt(NbtCompound nbt) {
        this.componentNbt = nbt.copy();
    }

    @Override
    protected NbtCompound writeComponentNbt(NbtCompound nbt) {
        return nbt.copyFrom(this.getComponentNbt());
    }
}
