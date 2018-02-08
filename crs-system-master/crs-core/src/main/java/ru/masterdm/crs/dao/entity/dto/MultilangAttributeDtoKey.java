package ru.masterdm.crs.dao.entity.dto;

import ru.masterdm.crs.domain.entity.attribute.MultilangAttribute;

/**
 * Store part of multilang data for {@code attributeMetaKey attribute of entity}.
 * @author Pavel Masalov
 */
public class MultilangAttributeDtoKey {

    private String attributeMetaKey;
    private String textRu, textEn;
    private MultilangAttribute multilangAttribute;

    /**
     * Returns attribute metakey value.
     * @return attribute metakey value
     */
    public String getAttributeMetaKey() {
        return attributeMetaKey;
    }

    /**
     * Sets attribute metakey value.
     * @param attributeMetaKey attribute metakey value
     */
    public void setAttributeMetaKey(String attributeMetaKey) {
        this.attributeMetaKey = attributeMetaKey;
    }

    /**
     * Returns ru text value from CLOB.
     * @return ru text value from CLOB
     */
    public String getTextRu() {
        return textRu;
    }

    /**
     * Sets ru text value for CLOB.
     * @param textRu ru text value for CLOB
     */
    public void setTextRu(String textRu) {
        this.textRu = textRu;
    }

    /**
     * Returns en text value for CLOB.
     * @return en text value for CLOB
     */
    public String getTextEn() {
        return textEn;
    }

    /**
     * Sets en text value for CLOB.
     * @param textEn en text value for CLOB
     */
    public void setTextEn(String textEn) {
        this.textEn = textEn;
    }

    /**
     * Returns multilang attribute.
     * @return multilang attribute
     */
    public MultilangAttribute getMultilangAttribute() {
        return multilangAttribute;
    }

    /**
     * Sets multilang attribute.
     * @param multilangAttribute multilang attribute
     */
    public void setMultilangAttribute(MultilangAttribute multilangAttribute) {
        this.multilangAttribute = multilangAttribute;
    }
}
