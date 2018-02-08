package ru.masterdm.crs.domain.entity.meta;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

/**
 * User tel number's meta attributes.
 * @author Alexey Kirilchev
 */
public enum UserTelAttributeMeta implements EmbeddedAttributeMeta {
    /** User tel. */
    TEL_NUMBER;

    @Override
    public String getKey() {
        return METADATA_KEY + KEY_DELIMITER + this.name();
    }

    public static final String METADATA_KEY = "USER_TEL";
}
