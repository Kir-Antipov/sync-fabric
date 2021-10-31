package dev.kir.sync.util.reflect;

import dev.kir.sync.util.function.FunctionUtil;

import java.lang.reflect.Method;
import java.util.Optional;

public final class ClassUtil {
    public static Optional<Method> getMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        return FunctionUtil.tryInvoke(() -> type.getMethod(name, parameterTypes));
    }
}