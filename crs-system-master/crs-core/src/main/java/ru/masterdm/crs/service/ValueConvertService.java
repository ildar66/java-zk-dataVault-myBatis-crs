package ru.masterdm.crs.service;

import ru.masterdm.crs.domain.entity.meta.AttributeMeta;

/**
 * Provide service to convert various input values into attribute proper datatype.
 * @author Pavel Masalov
 */
public interface ValueConvertService {

    /**
     * Convert value to type acceptable by type of attribute.
     * @param attributeMeta attribute type
     * @param sourceValue input value
     * @return proper value
     */
    Object convert(AttributeMeta attributeMeta, Object sourceValue);
}
