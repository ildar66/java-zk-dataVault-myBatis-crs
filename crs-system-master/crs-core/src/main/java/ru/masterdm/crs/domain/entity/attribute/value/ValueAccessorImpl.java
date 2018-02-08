package ru.masterdm.crs.domain.entity.attribute.value;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Value accessor that holds getter and setter.
 * @param <T> value data type
 * @author Pavel Masalov
 */
public class ValueAccessorImpl<T> implements ValueAccessor<T> {

    protected Consumer<T> setter;
    protected Supplier<T> getter;

    /**
     * Construct accessor with getter and setter.
     * @param setter value setter
     * @param getter value getter
     */
    public ValueAccessorImpl(Consumer<T> setter, Supplier<T> getter) {
        this.setter = setter;
        this.getter = getter;
    }

    @Override
    public Consumer<T> getSetter() {
        return setter;
    }

    @Override
    public Supplier<T> getGetter() {
        return getter;
    }
}
