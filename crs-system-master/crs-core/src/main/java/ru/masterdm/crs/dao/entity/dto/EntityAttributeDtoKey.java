package ru.masterdm.crs.dao.entity.dto;

import java.time.LocalDateTime;

import ru.masterdm.crs.domain.entity.attribute.EntityAttribute;

/**
 * Store part of entity reference data for {@code attributeMetaKey attribute of entity}.
 * @author Pavel Masalov
 */
public class EntityAttributeDtoKey {

    private String attributeMetaKey;
    private Long linkId; // just for result mapping to correlate with entity attribute. Don't used outside of result mapping
    private LocalDateTime ts;
    private EntityAttribute entityAttribute;

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
     * Returns entity reference attribute.
     * @return entity reference attribute
     */
    public EntityAttribute getEntityAttribute() {
        return entityAttribute;
    }

    /**
     * Sets entity reference attribute.
     * @param entityAttribute entity reference attribute
     */
    public void setEntityAttribute(EntityAttribute entityAttribute) {
        this.entityAttribute = entityAttribute;
    }

    /**
     * Returns link id.
     * @return link id
     */
    public Long getLinkId() {
        return linkId;
    }

    /**
     * Sets link id.
     * @param linkId link id
     */
    public void setLinkId(Long linkId) {
        this.linkId = linkId;
    }

    /**
     * Returns TS mark.
     * @return TS mark
     */
    public LocalDateTime getTs() {
        return ts;
    }

    /**
     * Sets TS mark.
     * @param ts TS mark
     */
    public void setTs(LocalDateTime ts) {
        this.ts = ts;
    }
}
