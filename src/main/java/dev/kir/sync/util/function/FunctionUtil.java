package dev.kir.sync.util.function;

import java.util.Optional;

/**
 * I want to see that fucking moron who thought checked exceptions were a good idea.
 *
 * "Yes please, force me to wrap this method call with try-catch!"
 * "Even though no exception can occur here, just fucking do it!"
 * - Statements dreamed up by the utterly deranged.
 */
public final class FunctionUtil {
    public static <T> Optional<T> tryInvoke(ThrowableSupplier<T> func) {
        try {
            return Optional.ofNullable(func.get());
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

    /**
     * Can you imagine creating an "invoke" function to fucking invoke you function?
     * This Java is an absolute shitfuckery, there are not enough words in this world
     * to describe how much I hate this parody of a high-level programming language.
     */
    public static <T> T invoke(ThrowableSupplier<T> func) {
        try {
            return func.get();
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}