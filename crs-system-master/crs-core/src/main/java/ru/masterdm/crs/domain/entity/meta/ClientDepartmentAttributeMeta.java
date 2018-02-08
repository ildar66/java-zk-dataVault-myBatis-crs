package ru.masterdm.crs.domain.entity.meta;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

/**
 * Client portal department meta attributes.
 * @author Igor Matushak
 */
public enum ClientDepartmentAttributeMeta implements EmbeddedAttributeMeta {

    /** Name. */
    NAME,
    /** Full Name. */
    FULLNAME;

    public static final String METADATA_KEY = "CLIENT_DEPARTMENT";

    @Override
    public String getKey() {
        return METADATA_KEY + KEY_DELIMITER + this.name();
    }
}