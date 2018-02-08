package ru.masterdm.crs.domain.entity.attribute;

import ru.masterdm.crs.domain.entity.attribute.value.ValueAccessor;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;

/**
 * Boolean attribute.
 * @author Pavel Masalov
 */
public class BooleanAttribute extends InTableAttribute<Boolean> {

    /**
     * Construct date attribute with known meta type.
     * @param meta attribute metadata
     */
    public BooleanAttribute(AttributeMeta meta) {
        super(meta);
    }

    /**
     * Construct boolean attribute with known meta type and value.
     * @param meta attribute metadata
     * @param valueAccessor attribute value accessor
     */
    public BooleanAttribute(AttributeMeta meta, ValueAccessor<Boolean> valueAccessor) {
        super(meta, valueAccessor);
    }

    @Override
    public void setValue(Boolean value) {
        // for  java.lang.ClassCastException: ... cannot be cast to ...
        super.setValue(value);
    }
}
