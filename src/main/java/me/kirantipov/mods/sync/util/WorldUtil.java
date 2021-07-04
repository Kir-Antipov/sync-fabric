package me.kirantipov.mods.sync.util;

import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.stream.StreamSupport;

public final class WorldUtil {
    public static Identifier getId(World world) {
        return world.getRegistryKey().getValue();
    }

    public static boolean isOf(Identifier id, World world) {
        return world.getRegistryKey().getValue().equals(id);
    }

    public static <T extends World> Optional<T> findWorld(Iterable<T> world, Identifier id) {
        return StreamSupport.stream(world.spliterator(), false).filter(x -> isOf(id, x)).findAny();
    }
}