package dev.kir.sync.util.nbt;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.NbtCompound;

import java.util.function.BiConsumer;

public class NbtSerializerFactory<T> {
    private final Iterable<BiConsumer<T, NbtCompound>> readers;
    private final Iterable<BiConsumer<T, NbtCompound>> writers;

    public NbtSerializerFactory(Iterable<BiConsumer<T, NbtCompound>> readers, Iterable<BiConsumer<T, NbtCompound>> writers) {
        this.readers = ImmutableList.copyOf(readers);
        this.writers = ImmutableList.copyOf(writers);
    }

    public NbtSerializer<T> create(T target) {
        return new NbtSerializer<>(target, this.readers, this.writers);
    }
}
