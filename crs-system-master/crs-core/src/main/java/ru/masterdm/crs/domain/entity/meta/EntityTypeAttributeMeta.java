package ru.masterdm.crs.domain.entity.meta;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

/**
 * Entity type meta attributes.
 * @author Pavel Masalov
 */
public enum EntityTypeAttributeMeta implements EmbeddedAttributeMeta {
    /** Russian name. */
    NAME_RU,
    /** English name. */
    NAME_EN,
    /** Allowed business actions. */
    BUSINESS_ACTION;

    public static final String METADATA_KEY = "ENTITY_TYPE";

    @Override
    public String getKey() {
        return METADATA_KEY + KEY_DELIMITER + this.name();
    }
}
