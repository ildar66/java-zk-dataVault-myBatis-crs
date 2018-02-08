package ru.masterdm.crs.domain.entity.meta;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

/**
 * Calculation profile meta attributes.
 * @author Pavel Masalov
 */
public enum CalculationProfileAttributeMeta implements EmbeddedAttributeMeta {
    /** Name. */
    NAME;

    public static final String METADATA_KEY = "CALC_PROFILE";

    @Override
    public String getKey() {
        return METADATA_KEY + KEY_DELIMITER + this.name();
    }
}
