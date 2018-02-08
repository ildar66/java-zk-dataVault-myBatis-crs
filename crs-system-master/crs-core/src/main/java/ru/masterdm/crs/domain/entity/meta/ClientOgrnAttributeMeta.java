package ru.masterdm.crs.domain.entity.meta;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

/**
 * Client OGRN meta attributes.
 * @author Alexey Chalov
 */
public enum ClientOgrnAttributeMeta implements EmbeddedAttributeMeta {

    /** Registration number. */
    REG_NUM,
    /** Country. */
    COUNTRY;

    public static final String METADATA_KEY = "CLIENT_OGRN";

    @Override
    public String getKey() {
        return METADATA_KEY + KEY_DELIMITER + this.name();
    }
}
