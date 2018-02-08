package ru.masterdm.crs.domain.entity.meta;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

/**
 * Favorites meta attributes.
 * @author Alexey Kirilchev
 */
public enum FavoritesAttributeMeta implements EmbeddedAttributeMeta {
    /** Client. */
    CLIENT,
    /** Client group. */
    CLIENT_GROUP,
    /** Calculation model. */
    CALC_MODEL,
    /** Calculation. */
    CALC,
    /** User. */
    USER;

    public static final String METADATA_KEY = "FAVORITES";

    @Override
    public String getKey() {
        return METADATA_KEY + KEY_DELIMITER + this.name();
    }
}
