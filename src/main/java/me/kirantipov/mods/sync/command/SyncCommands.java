package me.kirantipov.mods.sync.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.kirantipov.mods.sync.Sync;
import me.kirantipov.mods.sync.util.reflect.Activator;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.HashSet;
import java.util.Set;

public class SyncCommands {
    private static final Set<Command> COMMANDS = new HashSet<>();

    public static void init() {
        register(GhostShellsCommand.class);
    }

    private static <T extends Command> void register(Class<T> type) {
        COMMANDS.add(Activator.createInstance(type));
    }

    private static void init(MinecraftServer server) {
        LiteralArgumentBuilder<ServerCommandSource> commandBuilder = LiteralArgumentBuilder.literal(Sync.MOD_ID);
        commandBuilder.requires(x -> COMMANDS.stream().anyMatch(c -> c.hasPermissions(x)));

        for (Command commandInfo : COMMANDS) {
            LiteralArgumentBuilder<ServerCommandSource> command = CommandManager.literal(commandInfo.getName());
            command.requires(commandInfo::hasPermissions);
            commandInfo.build(command);
            commandBuilder.then(command);
        }

        CommandDispatcher<ServerCommandSource> commandDispatcher = server.getCommandManager().getDispatcher();
        commandDispatcher.register(commandBuilder);
    }

    static {
        ServerLifecycleEvents.SERVER_STARTING.register(SyncCommands::init);
    }
}