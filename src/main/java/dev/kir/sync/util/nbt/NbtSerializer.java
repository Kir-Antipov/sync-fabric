package dev.kir.sync.util.nbt;

import net.minecraft.nbt.NbtCompound;

import java.util.function.BiConsumer;

public class NbtSerializer<T> {
    private final T target;
    private final Iterable<BiConsumer<T, NbtCompound>> readers;
    private final Iterable<BiConsumer<T, NbtCompound>> writers;

    public NbtSerializer(T target, Iterable<BiConsumer<T, NbtCompound>> readers, Iterable<BiConsumer<T, NbtCompound>> writers) {
        this.target = target;
        this.readers = readers;
        this.writers = writers;
    }

    public void readNbt(NbtCompound nbt) {
        for (BiConsumer<T, NbtCompound> x : readers) {
            x.accept(this.target, nbt);
        }
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        for (BiConsumer<T, NbtCompound> x : writers) {
            x.accept(this.target, nbt);
        }
        return nbt;
    }
}
