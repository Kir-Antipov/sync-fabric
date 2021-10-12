package me.kirantipov.mods.sync.compat.requiem;

import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import me.kirantipov.mods.sync.api.event.PlayerSyncEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class RequiemCompat implements ModInitializer {
    @Override
    public void onInitialize() {
        if (!FabricLoader.getInstance().isModLoaded("requiemapi")) {
            return;
        }

        final Text PLAYER_IS_UNDEAD = new TranslatableText("event.sync.any.fail.undead");
        PlayerSyncEvents.ALLOW_SHELL_CONSTRUCTION.register((player, __) -> isUndead(player) ? () -> PLAYER_IS_UNDEAD : null);
        PlayerSyncEvents.ALLOW_SHELL_SELECTION.register((player, __) -> isUndead(player) ? () -> PLAYER_IS_UNDEAD : null);
        PlayerSyncEvents.ALLOW_SYNCING.register((player, __) -> isUndead(player) ? () -> PLAYER_IS_UNDEAD : null);
    }

    private static boolean isUndead(PlayerEntity player) {
        return PossessionComponent.get(player).isPossessionOngoing() || RemnantComponent.get(player).isIncorporeal();
    }
}
