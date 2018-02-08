package ru.masterdm.crs.domain.entity;

import ru.masterdm.crs.domain.entity.meta.EntityMeta;

/**
 * Entities that support its metadata.
 * @author Pavel Masalov
 */
public interface MetadataSupport {

    /**
     * Get object metadata.
     * @return metadata
     */
    EntityMeta getMeta();

    /**
     * Set object metadata.
     * @param meta metadata
     */
    void setMeta(EntityMeta meta);
}
