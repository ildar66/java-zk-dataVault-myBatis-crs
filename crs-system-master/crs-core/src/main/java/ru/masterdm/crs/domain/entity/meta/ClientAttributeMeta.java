package ru.masterdm.crs.domain.entity.meta;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

/**
 * Client's meta attributes.
 * @author Igor Matushak
 */
public enum ClientAttributeMeta implements EmbeddedAttributeMeta {
    /** Full name. */
    FULL_NAME,
    /** Name. */
    NAME,
    /** OPF. */
    OPF,
    /** Country. */
    COUNTRY,
    /** Category. */
    CATEGORY,
    /** Segment. */
    SEGMENT,
    /** Industry. */
    INDUSTRY,
    /** OGRN. */
    OGRN,
    /** Client type. */
    CLIENT_TYPE,
    /** Client group. */
    CLIENT_GROUP,
    /** Client INN. */
    CLIENT_INN,
    /** Department. */
    DEPARTMENT,
    /** Client Department. */
    CLIENT_DEPARTMENT;

    public static final String METADATA_KEY = "CLIENT";

    @Override
    public String getKey() {
        return METADATA_KEY + KEY_DELIMITER + this.name();
    }
}
