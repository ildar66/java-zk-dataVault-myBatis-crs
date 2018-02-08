package ru.masterdm.crs.domain.entity.meta;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

/**
 * Client category meta attributes.
 * @author Alexey Chalov
 */
public enum ClientCategoryAttributeMeta implements EmbeddedAttributeMeta {

    /** Name. */
    NAME;

    public static final String METADATA_KEY = "CLIENT_CATEGORY";

    @Override
    public String getKey() {
        return METADATA_KEY + KEY_DELIMITER + this.name();
    }
}
