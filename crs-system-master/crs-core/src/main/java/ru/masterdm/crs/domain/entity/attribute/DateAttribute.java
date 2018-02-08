package ru.masterdm.crs.domain.entity.attribute;

import java.time.LocalDate;

import ru.masterdm.crs.domain.entity.attribute.value.ValueAccessor;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;

/**
 * Date attribute.
 * @author Pavel Masalov
 */
public class DateAttribute extends InTableAttribute<LocalDate> {

    /**
     * Construct date attribute with known meta type.
     * @param meta attribute metadata
     */
    public DateAttribute(AttributeMeta meta) {
        super(meta);
    }

    /**
     * Construct date attribute with known meta type and value.
     * @param meta attribute metadata
     * @param valueAccessor attribute value accessor
     */
    public DateAttribute(AttributeMeta meta, ValueAccessor<LocalDate> valueAccessor) {
        super(meta, valueAccessor);
    }

    @Override
    public void setValue(LocalDate value) {
        // for  java.lang.ClassCastException: ... cannot be cast to ...
        super.setValue(value);
    }
}
