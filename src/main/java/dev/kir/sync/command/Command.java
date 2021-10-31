package dev.kir.sync.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;

public interface Command {
    String getName();

    boolean hasPermissions(ServerCommandSource commandSource);

    void build(ArgumentBuilder<ServerCommandSource, ?> builder);
}