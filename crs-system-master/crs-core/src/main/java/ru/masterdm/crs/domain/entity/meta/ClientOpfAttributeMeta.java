package ru.masterdm.crs.domain.entity.meta;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

/**
 * Client OPF meta attributes.
 * @author Alexey Chalov
 */
public enum ClientOpfAttributeMeta implements EmbeddedAttributeMeta {

    /** Name. */
    NAME,
    /** Country. */
    COUNTRY;

    public static final String METADATA_KEY = "CLIENT_OPF";

    @Override
    public String getKey() {
        return METADATA_KEY + KEY_DELIMITER + this.name();
    }
}
