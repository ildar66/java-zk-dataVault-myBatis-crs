package ru.masterdm.crs.service.entity.meta.ddl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ru.masterdm.crs.dao.entity.meta.MetadataDao;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.DsqlNames;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.service.EntityMetaService;

/**
 * Helpers to create DDL/DSQL names implementation.
 * @author Pavel Masalov
 */
@Service
public class DsqlNamesServiceImpl implements DsqlNamesService {

    @Autowired
    private EntityMetaService entityMetaService;
    @Autowired
    private MetadataDao metadataDao;

    @Value("#{config['ddl.hub.sys.prefix']}")
    private String hubSysPrefix;
    @Value("#{config['ddl.satellite.sys.prefix']}")
    private String satelliteSysPrefix;
    @Value("#{config['ddl.hub.prefix']}")
    private String hubPrefix;
    @Value("#{config['ddl.satellite.prefix']}")
    private String satellitePrefix;
    @Value("#{config['ddl.sequence.postfix']}")
    private String sequencePostfix;
    @Value("#{config['ddl.link.prefix']}")
    private String linkPrefix;
    @Value("#{config['ddl.link.satellite.prefix']}")
    private String linkSatellitePrefix;

    @Override
    public DsqlNames getNames(EntityMeta entityMeta, AttributeMeta attributeMeta) {
        return getNames(entityMeta, attributeMeta, metadataDao.getSysTimestamp());
    }

    @Override
    public DsqlNames getNames(EntityMeta entityMeta, AttributeMeta attributeMeta, LocalDateTime ldts) {
        DsqlNames dsqlNames =
                attributeMeta.getType() != AttributeType.REFERENCE
                ? new DsqlNames(entityMeta, attributeMeta)
                : new DsqlNames(entityMeta, attributeMeta, entityMetaService.getEntityMetaByKey(attributeMeta.getEntityKey(), ldts));
        initNames(dsqlNames);
        return dsqlNames;
    }

    @Override
    public DsqlNames getNames(EntityMeta entityMeta) {
        DsqlNames dsqlNames = new DsqlNames(entityMeta);
        initNames(dsqlNames);
        return dsqlNames;
    }

    /**
     * DsqlNames member initializer.
     * @param dsqlNames dsqlNames object to initialise
     */
    private void initNames(DsqlNames dsqlNames) {
        dsqlNames.setHubSysPrefix(hubSysPrefix);
        dsqlNames.setSatelliteSysPrefix(satelliteSysPrefix);

        if (dsqlNames.getEntityMeta().isSystemObject()) {
            dsqlNames.setHubPrefix(hubSysPrefix);
            dsqlNames.setSatellitePrefix(satelliteSysPrefix);
        } else {
            dsqlNames.setHubPrefix(hubPrefix);
            dsqlNames.setSatellitePrefix(satellitePrefix);
        }

        if (dsqlNames.isReferenceEntity()) {
            if (dsqlNames.getReferencedEntityMeta().isSystemObject()) {
                dsqlNames.setReferencedHubPrefix(hubSysPrefix);
                dsqlNames.setReferencedSatellitePrefix(satelliteSysPrefix);
            } else {
                dsqlNames.setReferencedHubPrefix(hubPrefix);
                dsqlNames.setReferencedSatellitePrefix(satellitePrefix);
            }
        }

        dsqlNames.setLinkPrefix(linkPrefix);
        dsqlNames.setLinkSatellitePrefix(linkSatellitePrefix);
        dsqlNames.setSequencePostfix(sequencePostfix);
    }
}
