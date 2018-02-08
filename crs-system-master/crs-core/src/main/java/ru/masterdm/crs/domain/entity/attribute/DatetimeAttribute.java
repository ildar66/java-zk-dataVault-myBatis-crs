package ru.masterdm.crs.domain.entity.attribute;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import ru.masterdm.crs.domain.entity.attribute.value.ValueAccessor;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;

/**
 * Date and time attribute.
 * @author Pavel Masalov
 */
public class DatetimeAttribute extends InTableAttribute<LocalDateTime> {

    /**
     * Construct datetime attribute with known meta type.
     * @param meta attribute metadata
     */
    public DatetimeAttribute(AttributeMeta meta) {
        super(meta);
    }

    /**
     * Construct datetime attribute with known meta type and value.
     * @param meta attribute metadata
     * @param valueAccessor attribute value accessor
     */
    public DatetimeAttribute(AttributeMeta meta, ValueAccessor<LocalDateTime> valueAccessor) {
        super(meta, valueAccessor);
    }

    @Override
    public void setValue(LocalDateTime value) {
        // for  java.lang.ClassCastException: ... cannot be cast to ...
        if (value != null)
            super.setValue(value.truncatedTo(ChronoUnit.SECONDS));
        else
            super.setValue(null);
    }
}
