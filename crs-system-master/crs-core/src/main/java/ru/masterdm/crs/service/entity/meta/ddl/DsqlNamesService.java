package ru.masterdm.crs.service.entity.meta.ddl;

import java.time.LocalDateTime;

import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.DsqlNames;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;

/**
 * Define helpers to create DDL/DSQL names.
 * @author Pavel Masalov
 */
public interface DsqlNamesService {

    /**
     * Get naming generator by entity and attribute metadata.
     * @param entityMeta entity metadata
     * @param attributeMeta attribute metadata
     * @return naming service
     */
    DsqlNames getNames(EntityMeta entityMeta, AttributeMeta attributeMeta);

    /**
     * Get naming generator by entity and attribute metadata.
     * @param entityMeta entity metadata
     * @param attributeMeta attribute metadata
     * @param ldts load datetime
     * @return naming service
     */
    DsqlNames getNames(EntityMeta entityMeta, AttributeMeta attributeMeta, LocalDateTime ldts);

    /**
     * Get naming generator by entity metadata.
     * @param entityMeta entity metadata
     * @return naming service
     */
    DsqlNames getNames(EntityMeta entityMeta);

}
