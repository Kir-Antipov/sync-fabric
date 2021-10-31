package dev.kir.sync.util.function;

@FunctionalInterface
public interface ThrowableSupplier<T> {
    T get() throws Throwable;
}
