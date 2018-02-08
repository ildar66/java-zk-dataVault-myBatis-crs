package ru.masterdm.crs.domain.entity.attribute;

import ru.masterdm.crs.domain.entity.attribute.value.ValueAccessor;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;

/**
 * String attribute.
 * @author Pavel Masalov
 */
public class StringAttribute extends InTableAttribute<String> {

    /**
     * Construct string attribute with known meta type.
     * @param meta attribute metadata
     */
    public StringAttribute(AttributeMeta meta) {
        super(meta);
    }

    /**
     * Construct string attribute with known meta type and value.
     * @param meta attribute metadata
     * @param valueAccessor attribute value accessor
     */
    public StringAttribute(AttributeMeta meta, ValueAccessor<String> valueAccessor) {
        super(meta, valueAccessor);
    }

    @Override
    public void setValue(String value) {
        // for java.lang.ClassCastException: ... cannot be cast to ...
        super.setValue(value);
    }
}
