package ru.masterdm.crs.domain;

import java.io.Serializable;

import ru.masterdm.crs.domain.entity.meta.AttributeLocale;

/**
 * Multi languages description.
 * @author Sergey Valiev
 */
public class MultilangDescription implements Serializable {

    private static final long serialVersionUID = 1L;

    private String descriptionRu;
    private String descriptionEn;

    /**
     * Default object constructor.
     */
    public MultilangDescription() {
    }

    /**
     * Value object constructor.
     * @param descriptionRu russian descriptor
     * @param descriptionEn english descriptor
     */
    public MultilangDescription(String descriptionRu, String descriptionEn) {
        this.descriptionRu = descriptionRu;
        this.descriptionEn = descriptionEn;
    }

    /**
     * Returns RU description.
     * @return RU description
     */
    public String getDescriptionRu() {
        return descriptionRu;
    }

    /**
     * Sets RU description.
     * @param descriptionRu RU description
     */
    public void setDescriptionRu(String descriptionRu) {
        this.descriptionRu = descriptionRu;
    }

    /**
     * Returns EN description.
     * @return EN description
     */
    public String getDescriptionEn() {
        return descriptionEn;
    }

    /**
     * Sets EN description.
     * @param descriptionEn EN description
     */
    public void setDescriptionEn(String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }

    /**
     * Returns localized description.
     * @param locale locale
     * @return localized description
     */
    public String getDescription(AttributeLocale locale) {
        AttributeLocale attributeLocale = (locale == null) ? AttributeLocale.EN : locale;
        switch (attributeLocale) {
            case RU:
                return getDescriptionRu();
            default:
                return getDescriptionEn();
        }
    }

    /**
     * Return empty indicator.
     * @return true if object contains no strings
     */
    public boolean isEmpty() {
        return (getDescriptionEn() == null || getDescriptionEn().isEmpty()) && (getDescriptionRu() == null || getDescriptionRu().isEmpty());
    }

}