package ru.masterdm.crs.service.entity.meta;

import ru.masterdm.crs.domain.entity.meta.EntityMeta;

/**
 * Data Vault structure management interface.
 * @author Alexey Chalov
 */
public interface DvStructureManagement {

    /**
     * Creates hub, satellite and links using metadata passed.
     * @param entityMeta {@link EntityMeta} instance
     */
    void create(EntityMeta entityMeta);

    /**
     * Updates satellite, adds new links using metadata change.
     * @param entityMetaOld old {@link EntityMeta} instance
     * @param entityMetaNew new {@link EntityMeta} instance
     */
    void update(EntityMeta entityMetaOld, EntityMeta entityMetaNew);
}
