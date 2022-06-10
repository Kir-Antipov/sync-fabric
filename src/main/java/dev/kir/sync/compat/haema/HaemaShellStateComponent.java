package dev.kir.sync.compat.haema;

import com.williambl.haema.component.VampireComponent;
import dev.kir.sync.compat.cca.CCAShellStateComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class HaemaShellStateComponent extends CCAShellStateComponent {
    public HaemaShellStateComponent() {
        this(null);
    }

    public HaemaShellStateComponent(@Nullable ServerPlayerEntity player) {
        super(player, VampireComponent.Companion.getEntityKey());
    }
}
