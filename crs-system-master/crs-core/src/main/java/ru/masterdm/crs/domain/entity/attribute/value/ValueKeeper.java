package ru.masterdm.crs.domain.entity.attribute.value;

/**
 * Used to hold value directly inside the object.
 * Used in attributes that hold value inside itself.
 * @param <T> value data type
 * @author Pavel Masalov
 */
public class ValueKeeper<T> extends ValueAccessorImpl<T> {

    private T value;

    /**
     * Construct object with value and getter and setter.
     */
    public ValueKeeper() {
        super(null, null);
        this.setter = (T v) -> this.value = v;
        this.getter = () -> this.value;
        setter.accept(null);
    }

    /**
     * Construct value keeper with external value.
     * @param v value
     */
    public ValueKeeper(T v) {
        this();
        setter.accept(v);
    }
}
