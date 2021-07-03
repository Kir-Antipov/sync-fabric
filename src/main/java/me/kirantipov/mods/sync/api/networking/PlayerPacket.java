package me.kirantipov.mods.sync.api.networking;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * Represents a packet that can be used for communication between a client and a server.
 */
public interface PlayerPacket {
    /**
     * @return Identifier of the packet.
     */
    Identifier getId();

    /**
     * Writes packet data to the buffer.
     * @param buffer The buffer.
     */
    void write(PacketByteBuf buffer);

    /**
     * Reads packet data from the buffer.
     * @param buffer The buffer.
     */
    void read(PacketByteBuf buffer);

    /**
     * @return true if the packet can be executed in background;
     * otherwise, false.
     */
    default boolean isBackgroundTask() {
        return false;
    }

    /**
     * Sends the packet via the given packet sender.
     * @param sender The packet sender.
     */
    default void send(PacketSender sender) {
        PacketByteBuf buffer = PacketByteBufs.create();
        this.write(buffer);
        sender.sendPacket(this.getId(), buffer);
    }
}