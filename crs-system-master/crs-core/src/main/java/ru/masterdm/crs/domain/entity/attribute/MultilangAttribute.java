package ru.masterdm.crs.domain.entity.attribute;

import ru.masterdm.crs.domain.MultilangDescription;
import ru.masterdm.crs.domain.entity.DigestSupport;
import ru.masterdm.crs.domain.entity.attribute.value.ValueAccessor;
import ru.masterdm.crs.domain.entity.attribute.value.ValueKeeper;
import ru.masterdm.crs.domain.entity.meta.AttributeLocale;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;

/**
 * Store information of metalanguage text or string.
 * @author Pavel Masalov
 */
public class MultilangAttribute extends ExternalAttribute<String, MultilangDescription> implements DigestSupport {

    /**
     * Construct empty attribute.
     */
    public MultilangAttribute() {
        super(new ValueKeeper(new MultilangDescription()));
    }

    /**
     * Construct attribute with metadata and internally stored value.
     * @param meta attribute metadata
     */
    public MultilangAttribute(AttributeMeta meta) {
        super(meta, new ValueKeeper(new MultilangDescription()));
    }

    /**
     * Construct attribute with known meta.
     * @param meta attribute meta
     * @param valueAccessor value accessor
     */
    public MultilangAttribute(AttributeMeta meta, ValueAccessor valueAccessor) {
        super(meta, valueAccessor);
    }

    /**
     * Get {@link MultilangDescription} object.
     * @return multilang object
     */
    public MultilangDescription getMultilangDescription() {
        return getValueAccessor().get();
    }

    /**
     * Set {@link MultilangDescription} object.
     * @param multilangDescription multilang object
     */
    public void setMultilangDescription(MultilangDescription multilangDescription) {
        getValueAccessor().set(multilangDescription);
    }

    @Override
    public String getValue() {
        // setValue[Locale] should be used
        throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(String value) {
        // getValue[Locale] should be used
        throw new UnsupportedOperationException();
    }

    /**
     * Returns RU value of attribute.
     * @return RU value of attribute
     */
    public String getValueRu() {
        return getMultilangDescription().getDescriptionRu();
    }

    /**
     * Sets RU value of attribute.
     * @param valueRu RU value of attribute
     */
    public void setValueRu(String valueRu) {
        getMultilangDescription().setDescriptionRu(valueRu);
    }

    /**
     * Returns EN value of attribute.
     * @return EN value of attribute
     */
    public String getValueEn() {
        return getMultilangDescription().getDescriptionEn();
    }

    /**
     * Sets EN value of attribute.
     * @param valueEn EN value of attribute
     */
    public void setValueEn(String valueEn) {
        getMultilangDescription().setDescriptionEn(valueEn);
    }

    /**
     * Returns localized value.
     * @param locale locale
     * @return localized value
     */
    public String getValue(AttributeLocale locale) {
        AttributeLocale attributeLocale = (locale == null) ? AttributeLocale.EN : locale;
        switch (attributeLocale) {
            case RU:
                return getValueRu();
            default:
                return getValueEn();
        }
    }

    /**
     * Sets localized value.
     * @param value localized value
     * @param locale locale
     */
    public void setValue(String value, AttributeLocale locale) {
        AttributeLocale attributeLocale = (locale == null) ? AttributeLocale.EN : locale;
        switch (attributeLocale) {
            case RU:
                setValueRu(value);
                break;
            default:
                setValueEn(value);
        }
    }

    /**
     * Return empty state of object.
     * @return true if objects has not ro and en values
     */

    public boolean isEmpty() {
        return (getValueEn() == null || getValueEn().isEmpty()) && (getValueRu() == null || getValueRu().isEmpty());
    }

    @Override
    public String calcDigest() {
        return calcDigest(getValueEn(), getValueRu());
    }
}
