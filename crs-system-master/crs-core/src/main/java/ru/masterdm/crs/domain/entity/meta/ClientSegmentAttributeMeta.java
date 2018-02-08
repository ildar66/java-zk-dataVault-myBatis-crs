package ru.masterdm.crs.domain.entity.meta;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

/**
 * Client's segment meta attributes.
 * @author Alexey Chalov
 */
public enum ClientSegmentAttributeMeta implements EmbeddedAttributeMeta {
    /** Name. */
    NAME,
    /** Revenue min. */
    REVENUE_MIN,
    /** Revenue max. */
    REVENUE_MAX,
    /** Currency. */
    CURRENCY;

    public static final String METADATA_KEY = "CLIENT_SEGMENT";

    @Override
    public String getKey() {
        return METADATA_KEY + KEY_DELIMITER + this.name();
    }
}
