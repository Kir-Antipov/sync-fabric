package dev.kir.sync.util.reflect;

import dev.kir.sync.util.function.FunctionUtil;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Optional;
import java.util.function.Supplier;

public final class Activator {
    @SuppressWarnings("unchecked")
    public static <T> Optional<Constructor<T>> getPublicConstructor(Class<T> type) {
        Optional<Constructor<T>> emptyConstructor = FunctionUtil.tryInvoke(type::getConstructor);
        if (emptyConstructor.isPresent()) {
            return emptyConstructor;
        }

        Constructor<?>[] constructors = type.getConstructors();
        if (constructors.length != 0) {
            return Optional.of((Constructor<T>)constructors[0]);
        }

        return Optional.empty();
    }

    public static <T> Optional<Supplier<T>> createSupplier(Class<T> type) {
        Constructor<T> constructor = getPublicConstructor(type).orElse(null);
        if (constructor == null) {
            return Optional.empty();
        }

        if (constructor.getParameterCount() == 0) {
            return Optional.of(() -> FunctionUtil.invoke(constructor::newInstance));
        }

        Object[] parameters = getDefaultParameterValues(constructor);
        return Optional.of(() -> FunctionUtil.invoke(() -> constructor.newInstance(parameters)));
    }

    public static <T> T createInstance(Class<T> type) {
        return createSupplier(type).orElseThrow().get();
    }

    private static <T> Object[] getDefaultParameterValues(Constructor<T> constructor) {
        Parameter[] parameters = constructor.getParameters();
        Object[] defaultValues = new Object[parameters.length];
        for (int i = 0; i < defaultValues.length; ++i) {
            defaultValues[i] = getDefaultValue(parameters[i].getType());
        }
        return defaultValues;
    }

    private static Object getDefaultValue(Class<?> type) {
        return type.isPrimitive() ? Array.get(Array.newInstance(type, 1), 0) : null;
    }
}