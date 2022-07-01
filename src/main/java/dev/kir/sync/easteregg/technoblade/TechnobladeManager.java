package dev.kir.sync.easteregg.technoblade;

import dev.kir.sync.Sync;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TechnobladeManager {
    private static final Set<BlockPos> POSITIONS = new HashSet<>(List.of(
            new BlockPos(1, 6, 1999),
            new BlockPos(6, 1, 1999),
            new BlockPos(1999, 6, 1),

            new BlockPos(30, 6, 2022),
            new BlockPos(6, 30, 2022),
            new BlockPos(2022, 6, 30)
    ));

    public static void refreshTechnobladeStatus(Entity entity) {
        if (Sync.getConfig().isTechnoblade(entity.getUuid())) {
            TechnobladeManager.transformEntityIntoTechnoblade(entity, false);
        }
    }

    public static void refreshTechnobladeStatus(Entity entity, BlockPos pos) {
        if (entity instanceof TechnobladeTransformable && POSITIONS.contains(pos)) {
            TechnobladeManager.transformEntityIntoTechnoblade(entity, true);
        }
    }

    private static void transformEntityIntoTechnoblade(Entity entity, boolean shouldAnnounce) {
        if (((TechnobladeTransformable)entity).transformIntoTechnoblade()) {
            Sync.getConfig().addTechnoblade(entity.getUuid());

            if (shouldAnnounce && Sync.getConfig().allowTechnobladeAnnouncements()) {
                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(new TranslatableText("multiplayer.player.joined", ((TechnobladeTransformable)entity).asTechnoblade().getDisplayName()));
            }
        }
    }

    static {
        if (!Sync.getConfig().enableTechnobladeEasterEgg()) {
            Sync.getConfig().clearTechnobladeCache();
        }
    }
}
