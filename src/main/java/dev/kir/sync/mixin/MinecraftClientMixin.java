package dev.kir.sync.mixin;

import dev.kir.sync.client.gui.controller.DeathScreenController;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(value = MinecraftClient.class, priority = 1001)
abstract class MinecraftClientMixin {
    /**
     * `setScreen(null)` opens DeathScreen when the player is dead.
     * This method can prevent this from happening.
     */
    @Redirect(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isDead()Z", ordinal = 0), require = 1)
    private boolean isPlayerDead(ClientPlayerEntity player) {
        return player.isDead() && !DeathScreenController.isSuspended();
    }
}