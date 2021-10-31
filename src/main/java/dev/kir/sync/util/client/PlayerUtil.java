package dev.kir.sync.util.client;

import com.google.common.collect.Queues;
import dev.kir.sync.Sync;
import dev.kir.sync.util.WorldUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

@Environment(EnvType.CLIENT)
public final class PlayerUtil {
    private static final Identifier ANY_WORLD = Sync.locate("any_world");
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final ConcurrentMap<Identifier, ConcurrentLinkedQueue<PlayerUpdate>> UPDATES = new ConcurrentHashMap<>();

    public static void recordPlayerUpdate(PlayerUpdate playerUpdate) {
        recordPlayerUpdate(null, playerUpdate);
    }

    public static void recordPlayerUpdate(Identifier worldId, PlayerUpdate playerUpdate) {
        worldId = worldId == null ? ANY_WORLD : worldId;

        if (CLIENT.player != null && existsInTargetWorld(CLIENT.player, worldId)) {
            playerUpdate.onLoad(CLIENT.player, CLIENT.player.clientWorld, CLIENT);
        } else {
            UPDATES.computeIfAbsent(worldId, id -> Queues.newConcurrentLinkedQueue()).add(playerUpdate);
        }
    }

    private static boolean existsInTargetWorld(Entity entity, Identifier worldId) {
        return worldId == ANY_WORLD || WorldUtil.isOf(worldId, entity.world);
    }

    private static void executeUpdates(ClientPlayerEntity player, ClientWorld world, ConcurrentLinkedQueue<PlayerUpdate> queue) {
        if (queue == null) {
            return;
        }

        while (!queue.isEmpty()) {
            queue.poll().onLoad(player, world, CLIENT);
        }
    }

    static {
        ClientEntityEvents.ENTITY_LOAD.register((Entity entity, ClientWorld world) -> {
            if (entity == CLIENT.player) {
                executeUpdates(CLIENT.player, world, UPDATES.get(WorldUtil.getId(world)));
                executeUpdates(CLIENT.player, world, UPDATES.get(ANY_WORLD));
            }
        });
    }

    @FunctionalInterface
    public interface PlayerUpdate {
        void onLoad(ClientPlayerEntity player, ClientWorld world, MinecraftClient client);
    }
}