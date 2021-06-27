package me.kirantipov.mods.sync.util.nbt;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class NbtSerializerFactoryBuilder<TTarget> {
    private static final Map<Class<?>, BiFunction<NbtCompound, String, ?>> NBT_GETTERS;
    private static final Map<Class<?>, TriConsumer<NbtCompound, String, ?>> NBT_SETTERS;

    private final Collection<BiConsumer<TTarget, NbtCompound>> readers;
    private final Collection<BiConsumer<TTarget, NbtCompound>> writers;

    public NbtSerializerFactoryBuilder() {
        this.readers = new ArrayList<>();
        this.writers = new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public <TProperty> NbtSerializerFactoryBuilder<TTarget> add(Class<TProperty> type, String key, Function<TTarget, TProperty> getter, BiConsumer<TTarget, TProperty> setter) {
        if (getter != null) {
            TriConsumer<NbtCompound, String, TProperty> nbtSetter = (TriConsumer<NbtCompound, String, TProperty>)NBT_SETTERS.get(type);
            if (nbtSetter == null) {
                throw new UnsupportedOperationException();
            }
            this.writers.add((i, x) -> nbtSetter.accept(x, key, getter.apply(i)));
        }

        if (setter != null) {
            BiFunction<NbtCompound, String, TProperty> nbtGetter = (BiFunction<NbtCompound, String, TProperty>)NBT_GETTERS.get(type);
            if (nbtGetter == null) {
                throw new UnsupportedOperationException();
            }
            this.readers.add((i, x) -> setter.accept(i, nbtGetter.apply(x, key)));
        }

        return this;
    }

    public NbtSerializerFactory<TTarget> build() {
        return new NbtSerializerFactory<>(this.readers, this.writers);
    }

    private static BiFunction<NbtCompound, String, ?> getOrDefault(BiFunction<NbtCompound, String, ?> f) {
        return (nbt, key) -> nbt.contains(key) ? f.apply(nbt, key) : null;
    }

    private static TriConsumer<NbtCompound, String, ?> setIfNotNull(TriConsumer<NbtCompound, String, Object> f) {
        return (nbt, key, x) -> {
            if (x != null) {
                f.accept(nbt, key, x);
            }
        };
    }

    static {
        NBT_GETTERS = new HashMap<>();
        NBT_GETTERS.put(Boolean.class, getOrDefault(NbtCompound::getBoolean));
        NBT_GETTERS.put(Byte.class, getOrDefault(NbtCompound::getByte));
        NBT_GETTERS.put(Double.class, getOrDefault(NbtCompound::getDouble));
        NBT_GETTERS.put(Float.class, getOrDefault(NbtCompound::getFloat));
        NBT_GETTERS.put(Integer.class, getOrDefault(NbtCompound::getInt));
        NBT_GETTERS.put(Long.class, getOrDefault(NbtCompound::getLong));
        NBT_GETTERS.put(Short.class, getOrDefault(NbtCompound::getShort));
        NBT_GETTERS.put(String.class, getOrDefault(NbtCompound::getString));
        NBT_GETTERS.put(Identifier.class, getOrDefault((x, key) -> new Identifier(x.getString(key))));
        NBT_GETTERS.put(UUID.class, getOrDefault(NbtCompound::getUuid));
        NBT_GETTERS.put(NbtCompound.class, getOrDefault(NbtCompound::getCompound));
        NBT_GETTERS.put(NbtList.class, getOrDefault(NbtCompound::get));
        NBT_GETTERS.put(BlockPos.class, getOrDefault((nbt, key) -> {
            NbtCompound compound = nbt.getCompound(key);
            return new BlockPos(compound.getInt("x"), compound.getInt("y"), compound.getInt("z"));
        }));

        NBT_SETTERS = new HashMap<>();
        NBT_SETTERS.put(Boolean.class, setIfNotNull((nbt, key, x) -> nbt.putBoolean(key, (boolean)x)));
        NBT_SETTERS.put(Byte.class, setIfNotNull((nbt, key, x) -> nbt.putByte(key, (byte)x)));
        NBT_SETTERS.put(Double.class, setIfNotNull((nbt, key, x) -> nbt.putDouble(key, (double)x)));
        NBT_SETTERS.put(Float.class, setIfNotNull((nbt, key, x) -> nbt.putFloat(key, (float)x)));
        NBT_SETTERS.put(Integer.class, setIfNotNull((nbt, key, x) -> nbt.putInt(key, (int)x)));
        NBT_SETTERS.put(Long.class, setIfNotNull((nbt, key, x) -> nbt.putLong(key, (long)x)));
        NBT_SETTERS.put(Short.class, setIfNotNull((nbt, key, x) -> nbt.putShort(key, (short)x)));
        NBT_SETTERS.put(String.class, setIfNotNull((nbt, key, x) -> nbt.putString(key, (String)x)));
        NBT_SETTERS.put(Identifier.class, setIfNotNull((nbt, key, x) -> nbt.putString(key, x.toString())));
        NBT_SETTERS.put(UUID.class, setIfNotNull((nbt, key, x) -> nbt.putUuid(key, (UUID)x)));
        NBT_SETTERS.put(NbtCompound.class, setIfNotNull((nbt, key, x) -> nbt.put(key, (NbtCompound)x)));
        NBT_SETTERS.put(NbtList.class, setIfNotNull((nbt, key, x) -> nbt.put(key, (NbtList)x)));
        NBT_SETTERS.put(BlockPos.class, setIfNotNull((nbt, key, x) -> {
            BlockPos pos = (BlockPos)x;
            NbtCompound compound = new NbtCompound();
            compound.putInt("x", pos.getX());
            compound.putInt("y", pos.getY());
            compound.putInt("z", pos.getZ());
            nbt.put(key, compound);
        }));
    }

    @FunctionalInterface
    private interface TriConsumer<T, K, V> {
        void accept(T arg1, K arg2, V arg3);
    }
}
