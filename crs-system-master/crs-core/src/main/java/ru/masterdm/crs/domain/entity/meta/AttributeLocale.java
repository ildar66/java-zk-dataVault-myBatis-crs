package ru.masterdm.crs.domain.entity.meta;

import java.util.Locale;

/**
 * Available locales.
 * @author Sergey Valiev
 * @author Igor Matushak
 */
public enum AttributeLocale {
    /** RU locale. */
    RU,
    /** EN locale. */
    EN;

    /**
     * Returns locale by lang.
     * @param locale locale
     * @return locale by land
     */
    public static AttributeLocale getLocale(Locale locale) {
        AttributeLocale result = EN;
        if (locale == null) {
            return result;
        }

        try {
            result = AttributeLocale.valueOf(locale.getLanguage().toUpperCase());
        } catch (IllegalArgumentException e) {
            return result;
        }
        return result;
    }
}
