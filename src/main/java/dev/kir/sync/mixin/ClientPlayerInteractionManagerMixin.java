package dev.kir.sync.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    /**
     * I truly don't understand why Minecraft doesn't implement this check.
     * Long story short - if the player will try to attack themselves, they will be kicked from a server.
     * And the player actually tries to commit an act of masochism if an attack occurs after their death.
     */
    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void attackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        if (player == target) {
            ci.cancel();
        }
    }
}