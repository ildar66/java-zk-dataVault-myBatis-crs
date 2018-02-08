package ru.masterdm.crs.domain.entity.meta;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

/**
 * Client's INN meta attributes.
 * @author Igor Matushak
 */
public enum ClientInnAttributeMeta implements EmbeddedAttributeMeta {
    /** Tax id. */
    TAX_ID,
    /** Country. */
    COUNTRY;

    public static final String METADATA_KEY = "CLIENT_INN";

    @Override
    public String getKey() {
        return METADATA_KEY + KEY_DELIMITER + this.name();
    }
}
