package ru.masterdm.crs.dao.entity.meta.dto;

import java.util.List;

import ru.masterdm.crs.domain.entity.meta.AttributeMeta;

/**
 * Data transfer object for attribute's attribute metadata.
 * @author Pavel Masalov
 */
public class AttributeAttributesDto {
    private Long attributeId;
    private List<AttributeMeta> attributes;

    /**
     * Returns attribute primary id.
     * @return attribute primary id
     */
    public Long getAttributeId() {
        return attributeId;
    }

    /**
     * Sets parent attribute primary id.
     * @param attributeId attribute primary id
     */
    public void setAttributeId(Long attributeId) {
        this.attributeId = attributeId;
    }

    /**
     * Returns attribute's attribute list.
     * @return attribute's attribute list
     */
    public List<AttributeMeta> getAttributes() {
        return attributes;
    }

    /**
     * Sets attribute's attribute list.
     * @param attributes attribute's attribute list
     */
    public void setAttributes(List<AttributeMeta> attributes) {
        this.attributes = attributes;
    }
}
