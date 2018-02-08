package ru.masterdm.crs.dao.entity.meta.dto;

import java.util.List;

import ru.masterdm.crs.domain.entity.meta.AttributeMeta;

/**
 * Data transfer object to load attributes metadata.
 * @author Pavel Masalov
 */
public class EntityAttributeMetaDto {

    private Long entityId;
    private List<AttributeMeta> attributes;

    /**
     * Returns entity meta permanent id.
     * @return entity meta permanent id
     */
    public Long getEntityId() {
        return entityId;
    }

    /**
     * Sets entity meta permanent id.
     * @param entityId entity meta permanent id
     */
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    /**
     * Returns list of attribute metadatas.
     * @return list of attribute metadatas
     */
    public List<AttributeMeta> getAttributes() {
        return attributes;
    }

    /**
     * Sets list of attribute metadatas.
     * @param attributes list of attribute metadatas
     */
    public void setAttributes(List<AttributeMeta> attributes) {
        this.attributes = attributes;
    }
}
