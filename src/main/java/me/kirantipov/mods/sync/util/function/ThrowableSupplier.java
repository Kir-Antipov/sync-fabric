package me.kirantipov.mods.sync.util.function;

@FunctionalInterface
public interface ThrowableSupplier<T> {
    T get() throws Throwable;
}
