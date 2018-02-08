package ru.masterdm.crs.domain.entity.meta;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

/**
 * Client's group meta attributes.
 * @author Igor Matushak
 */
public enum ClientGroupAttributeMeta implements EmbeddedAttributeMeta {
    /** Name. */
    NAME,
    /** Full name. */
    FULL_NAME,
    /** Description. */
    DESCRIPTION,
    /** Segment. */
    SEGMENT,
    /** Industry. */
    INDUSTRY,
    /** Country code. */
    COUNTRY,
    /** VTB daughter. */
    VTB_DAUGHTER;

    public static final String METADATA_KEY = "CLIENT_GROUP";

    @Override
    public String getKey() {
        return METADATA_KEY + KEY_DELIMITER + this.name();
    }
}
