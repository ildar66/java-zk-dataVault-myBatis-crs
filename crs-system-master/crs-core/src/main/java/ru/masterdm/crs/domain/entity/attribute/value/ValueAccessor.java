package ru.masterdm.crs.domain.entity.attribute.value;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Interface ot implement logic for get and set attribute value.
 * @param <T> value data type
 * @author Pavel Masalov
 */
public interface ValueAccessor<T> {

    /**
     * Get setter of the value.
     * @return setter
     */
    Consumer<T> getSetter();

    /**
     * Get getter of the value.
     * @return getter
     */
    Supplier<T> getGetter();

    /**
     * Get getter null safe value.
     * @return value
     */
    default T get() {
        Supplier<T> g = getGetter();
        return g == null ? null : g.get();
    }

    /**
     * Set setter null safe value.
     * @param v value
     */
    default void set(T v) {
        Consumer<T> a = getSetter();
        if (a != null)
            a.accept(v);
    }
}
