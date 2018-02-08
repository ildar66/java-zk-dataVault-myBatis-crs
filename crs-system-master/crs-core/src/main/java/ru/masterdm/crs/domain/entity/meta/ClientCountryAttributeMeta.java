package ru.masterdm.crs.domain.entity.meta;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

/**
 * Client country meta attributes.
 * @author Alexey Chalov
 */
public enum ClientCountryAttributeMeta implements EmbeddedAttributeMeta {

    /** Name. */
    NAME;

    public static final String METADATA_KEY = "CLIENT_COUNTRY";

    @Override
    public String getKey() {
        return METADATA_KEY + KEY_DELIMITER + this.name();
    }
}
