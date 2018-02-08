package ru.masterdm.crs.domain.entity.meta;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

/**
 * Client's type meta attributes.
 * @author Igor Matushak
 */
public enum ClientTypeAttributeMeta implements EmbeddedAttributeMeta {

    /** Name. */
    NAME;

    public static final String METADATA_KEY = "CLIENT_TYPE";

    @Override
    public String getKey() {
        return METADATA_KEY + KEY_DELIMITER + this.name();
    }
}
