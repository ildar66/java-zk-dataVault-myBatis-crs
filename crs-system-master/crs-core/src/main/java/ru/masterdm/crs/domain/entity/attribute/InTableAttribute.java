package ru.masterdm.crs.domain.entity.attribute;

import ru.masterdm.crs.domain.entity.attribute.value.ValueAccessor;
import ru.masterdm.crs.domain.entity.attribute.value.ValueKeeper;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;

/**
 * Base class for all attributes that value stored as column at satellite table.
 * @param <T> Java type to be stored at attribute
 * @author Pavel Masalov
 */
public abstract class InTableAttribute<T> extends AbstractAttribute<T> {

    private ValueAccessor<T> valueAccessor;

    /**
     * Constructor for known meta.
     * @param meta attribute meta
     */
    public InTableAttribute(AttributeMeta meta) {
        this(meta, new ValueKeeper<>());
    }

    /**
     * Create intable attribute with value accessor.
     * @param meta attribute metadata
     * @param valueAccessor value accessor
     */
    public InTableAttribute(AttributeMeta meta, ValueAccessor<T> valueAccessor) {
        super(meta);
        this.valueAccessor = valueAccessor;
    }

    @Override
    public T getValue() {
        return valueAccessor.getGetter().get();
    }

    @Override
    public void setValue(T value) {
        this.valueAccessor.getSetter().accept(value);
    }

    /**
     * Sets set value accessor.
     * @param valueAccessor value accessor
     */
    public void setValueAccessor(ValueAccessor<T> valueAccessor) {
        this.valueAccessor = valueAccessor;
    }
}
