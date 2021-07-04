package me.kirantipov.mods.sync.api.networking;

import com.mojang.blaze3d.systems.RenderSystem;
import me.kirantipov.mods.sync.util.client.PlayerUtil;
import me.kirantipov.mods.sync.util.reflect.Activator;
import me.kirantipov.mods.sync.util.reflect.ClassUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Represents a packet that can be sent from a server to a client.
 */
public interface ClientPlayerPacket extends PlayerPacket {
    /**
     * Sends the packet to all players that are currently connected to the server.
     * @param server The server.
     */
    default void sendToAll(MinecraftServer server) {
        this.send(PlayerLookup.all(server));
    }

    /**
     * Sends the packet to the client.
     * @param player Player that should receive the packet.
     */
    default void send(ServerPlayerEntity player) {
        this.send(Stream.of(player));
    }

    /**
     * Sends the packet to the client.
     * @param players Players that should receive the packet.
     */
    default void send(Collection<ServerPlayerEntity> players) {
        this.send(players.stream());
    }

    /**
     * Sends the packet to the client.
     * @param players Players that should receive the packet.
     */
    default void send(Stream<ServerPlayerEntity> players) {
        Identifier id = this.getId();
        PacketByteBuf buffer = PacketByteBufs.create();
        this.write(buffer);
        players.forEach(player -> ServerPlayNetworking.send(player, id, buffer));
    }

    /**
     * @return true if the packet should be executed on the render thread;
     * otherwise, false.
     */
    default boolean isRenderTask() {
        return false;
    }

    /**
     * @return Identifier of the world the packet should be executed in.
     */
    @Nullable
    default Identifier getTargetWorldId() {
        return null;
    }

    /**
     * This method is called after the packet is delivered to the client side.
     *
     * @param client The client.
     * @param handler The network handler that received this packet
     * @param responseSender The packet sender that can be used to send a response.
     */
    @Environment(EnvType.CLIENT)
    default void execute(MinecraftClient client, ClientPlayNetworkHandler handler, PacketSender responseSender) {
        PlayerUtil.recordPlayerUpdate(this.getTargetWorldId(), (player, w, c) -> {
            if (this.isBackgroundTask()) {
                this.execute(client, player, handler, responseSender);
            } else if (this.isRenderTask()) {
                RenderSystem.recordRenderCall(() -> this.execute(client, player, handler, responseSender));
            } else {
                client.execute(() -> this.execute(client, player, handler, responseSender));
            }
        });
    }

    /**
     * This method is called after the packet is delivered to the client side.
     *
     * @param client The client.
     * @param player The player.
     * @param handler The network handler that received this packet
     * @param responseSender The packet sender that can be used to send a response.
     */
    @Environment(EnvType.CLIENT)
    default void execute(MinecraftClient client, ClientPlayerEntity player, ClientPlayNetworkHandler handler, PacketSender responseSender) { }

    /**
     * Registers a client side handler for the specified packet.
     * @param type Class of the packet.
     * @param <T> The packet.
     */
    @Environment(EnvType.CLIENT)
    static <T extends ClientPlayerPacket> void register(Class<T> type) {
        Supplier<T> supplier = Activator.createSupplier(type).orElseThrow();
        ClientPlayerPacket packet = supplier.get();
        boolean originalExecute = ClassUtil.getMethod(type, "execute", MinecraftClient.class, ClientPlayNetworkHandler.class, PacketSender.class).map(x -> "me.kirantipov.mods.sync.api.networking.ClientPlayerPacket".equals(x.getDeclaringClass().getName())).orElse(false);

        ClientPlayNetworking.registerGlobalReceiver(packet.getId(), (client, handler, buffer, responseSender) -> {
            ClientPlayerPacket localPacket = supplier.get();
            localPacket.read(buffer);
            if (originalExecute || localPacket.isBackgroundTask()) {
                localPacket.execute(client, handler, responseSender);
            } else if (localPacket.isRenderTask()) {
                RenderSystem.recordRenderCall(() -> localPacket.execute(client, handler, responseSender));
            } else {
                client.execute(() -> localPacket.execute(client, handler, responseSender));
            }
        });
    }
}