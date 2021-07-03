package me.kirantipov.mods.sync.api.networking;

import me.kirantipov.mods.sync.util.reflect.Activator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

/**
 * Represents a packet that can be sent from a client to a server.
 */
public interface ServerPlayerPacket extends PlayerPacket {
    /**
     * Sends the packet to the server.
     */
    @Environment(EnvType.CLIENT)
    default void send() {
        Identifier id = this.getId();
        PacketByteBuf buffer = PacketByteBufs.create();
        this.write(buffer);
        ClientPlayNetworking.send(id, buffer);
    }

    /**
     * This method is called after the packet is delivered to the server side.
     *
     * @param server The server.
     * @param player The player, who sent the packet from the client side.
     * @param handler The network handler that received this packet, representing the player, who sent the packet.
     * @param responseSender The packet sender that can be used to send a response.
     */
    void execute(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketSender responseSender);

    /**
     * Registers a server side handler for the specified packet.
     * @param type Class of the packet.
     * @param <T> The packet.
     */
    static <T extends ServerPlayerPacket> void register(Class<T> type) {
        Supplier<T> supplier = Activator.createSupplier(type).orElseThrow();
        ServerPlayerPacket packet = supplier.get();
        ServerPlayNetworking.registerGlobalReceiver(packet.getId(), (server, player, handler, buffer, responseSender) -> {
            ServerPlayerPacket localPacket = supplier.get();
            localPacket.read(buffer);
            if (localPacket.isBackgroundTask()) {
                localPacket.execute(server, player, handler, responseSender);
            } else {
                server.execute(() -> localPacket.execute(server, player, handler, responseSender));
            }
        });
    }
}