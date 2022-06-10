package dev.kir.sync.compat.requiem;

import dev.kir.sync.api.event.PlayerSyncEvents;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class RequiemCompat implements ModInitializer {
    @Override
    public void onInitialize() {
        if (!FabricLoader.getInstance().isModLoaded("requiem")) {
            return;
        }

        final Text PLAYER_IS_UNDEAD = Text.translatable("event.sync.any.fail.undead");
        PlayerSyncEvents.ALLOW_SHELL_CONSTRUCTION.register((player, __) -> isUndead(player) ? () -> PLAYER_IS_UNDEAD : null);
        PlayerSyncEvents.ALLOW_SHELL_SELECTION.register((player, __) -> isUndead(player) ? () -> PLAYER_IS_UNDEAD : null);
        PlayerSyncEvents.ALLOW_SYNCING.register((player, __) -> isUndead(player) ? () -> PLAYER_IS_UNDEAD : null);
    }

    private static boolean isUndead(PlayerEntity player) {
        return PossessionComponent.get(player).isPossessionOngoing() || RemnantComponent.get(player).isIncorporeal();
    }
}
