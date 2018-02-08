package ru.masterdm.crs.domain.entity.attribute;

import java.math.BigDecimal;

import ru.masterdm.crs.domain.entity.attribute.value.ValueAccessor;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;

/**
 * Number attribute.
 * @author Pavel Masalov
 */
public class NumberAttribute extends InTableAttribute<BigDecimal> {

    /**
     * Construct number attribute with known meta type.
     * @param meta attribute metadata
     */
    public NumberAttribute(AttributeMeta meta) {
        super(meta);
    }

    /**
     * Construct number attribute with known meta type and value.
     * @param meta attribute metadata
     * @param valueAccessor attribute value accessor
     */
    public NumberAttribute(AttributeMeta meta, ValueAccessor<BigDecimal> valueAccessor) {
        super(meta, valueAccessor);
    }

    @Override
    public void setValue(BigDecimal value) {
        // for  java.lang.ClassCastException: ... cannot be cast to ...
        super.setValue(value);
    }
}
