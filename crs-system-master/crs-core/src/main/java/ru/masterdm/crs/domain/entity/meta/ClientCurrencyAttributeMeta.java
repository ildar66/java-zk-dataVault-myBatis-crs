package ru.masterdm.crs.domain.entity.meta;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

/**
 * Client currency meta attributes.
 * @author Alexey Chalov
 */
public enum ClientCurrencyAttributeMeta implements EmbeddedAttributeMeta {

    /** Name. */
    NAME;

    public static final String METADATA_KEY = "CLIENT_CURRENCY";

    @Override
    public String getKey() {
        return METADATA_KEY + KEY_DELIMITER + this.name();
    }
}
