package ru.masterdm.crs.dao.entity.meta.dto;

import java.util.List;

import ru.masterdm.crs.domain.entity.meta.EntityMeta;

/**
 * Data transfer object to get "total count" with list of entities.
 * @author Pavel Masalov
 */
public class EntityMetaDto {

    private Long cc;
    private List<EntityMeta> entityMetas;

    /**
     * Returns "count" data.
     * @return "count" data
     */
    public Long getCc() {
        return cc;
    }

    /**
     * Sets "count" data.
     * @param cc "count" data
     */
    public void setCc(Long cc) {
        this.cc = cc;
    }

    /**
     * Returns list of entity meta.
     * @return list of entity meta
     */
    public List<EntityMeta> getEntityMetas() {
        return entityMetas;
    }

    /**
     * Sets list of entity meta.
     * @param entityMetas list of entity meta
     */
    public void setEntityMetas(List<EntityMeta> entityMetas) {
        this.entityMetas = entityMetas;
    }
}
