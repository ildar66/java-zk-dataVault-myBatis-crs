package ru.masterdm.crs.service.entity.meta;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ru.masterdm.crs.dao.entity.meta.MetadataDao;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.service.entity.meta.ddl.CreateHubDdlBuilder;
import ru.masterdm.crs.service.entity.meta.ddl.CreateHubSelfRefLinkDdlBuilder;
import ru.masterdm.crs.service.entity.meta.ddl.CreateHubSelfRefLinkSequenceDdlBuilder;
import ru.masterdm.crs.service.entity.meta.ddl.CreateHubSequenceDdlBuilder;
import ru.masterdm.crs.service.entity.meta.ddl.CreateLinkDdlBuilder;
import ru.masterdm.crs.service.entity.meta.ddl.CreateLinkSequenceDdlBuilder;
import ru.masterdm.crs.service.entity.meta.ddl.CreateSatelliteDdlBuilder;
import ru.masterdm.crs.service.entity.meta.ddl.CreateSatelliteSequenceDdlBuilder;

/**
 * {@link DvStructureManagement} service implementation.
 * @author Alexey Chalov
 */
@Service
public class DvStructureManagementImpl implements DvStructureManagement {

    private static final Logger LOG = LoggerFactory.getLogger(DvStructureManagementImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private MetadataDao metadataDao;
    @Autowired
    private CreateHubDdlBuilder createHubDdlBuilder;
    @Autowired
    private CreateHubSequenceDdlBuilder createHubSequenceDdlBuilder;
    @Autowired
    private CreateSatelliteDdlBuilder createSatelliteDdlBuilder;
    @Autowired
    private CreateSatelliteSequenceDdlBuilder createSatelliteSequenceDdlBuilder;
    @Autowired
    private CreateHubSelfRefLinkDdlBuilder createHubSelfRefLinkDdlBuilder;
    @Autowired
    private CreateHubSelfRefLinkSequenceDdlBuilder createHubSelfRefLinkSequenceDdlBuilder;
    @Autowired
    private CreateLinkDdlBuilder createLinkDdlBuilder;
    @Autowired
    private CreateLinkSequenceDdlBuilder createLinkSequenceDdlBuilder;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void create(EntityMeta entityMeta) {
        List<Pair<String, String>> totalDdl = new LinkedList<>();
        totalDdl.addAll(createHubDdlBuilder.build(entityMeta));
        totalDdl.addAll(createHubSequenceDdlBuilder.build(entityMeta));

        totalDdl.addAll(createSatelliteDdlBuilder.build(entityMeta));
        totalDdl.addAll(createSatelliteSequenceDdlBuilder.build(entityMeta));

        totalDdl.addAll(createHubSelfRefLinkDdlBuilder.build(entityMeta));
        totalDdl.addAll(createHubSelfRefLinkSequenceDdlBuilder.build(entityMeta));

        totalDdl.addAll(createLinkDdlBuilder.build(entityMeta));
        totalDdl.addAll(createLinkSequenceDdlBuilder.build(entityMeta));

        executeDdl(totalDdl);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void update(EntityMeta entityMetaOld, EntityMeta entityMetaNew) {
        List<AttributeMeta> addedAttributes = entityMetaNew.getAttributes().stream().filter(
                attr -> !entityMetaOld.getAttributes().contains(attr)
        ).collect(Collectors.toList());

        List<Pair<String, String>> totalDdl = new ArrayList<>();
        addedAttributes.stream().forEachOrdered(item -> {
            totalDdl.addAll(createSatelliteDdlBuilder.buildUpdate(entityMetaOld, item));
            totalDdl.addAll(createLinkDdlBuilder.buildUpdate(entityMetaOld, item));
            totalDdl.addAll(createLinkSequenceDdlBuilder.buildUpdate(entityMetaOld, item));
        });

        executeDdl(totalDdl);
    }

    /**
     * Executes DDL scripts. Tries to rollback executed scripts if error rise.
     * @param totalDdl list of create/drop script pairs
     */
    private void executeDdl(List<Pair<String, String>> totalDdl) {
        ReentrantLock lock = new ReentrantLock(true);
        lock.lock();
        int position = 0;
        try {
            for (; position < totalDdl.size(); position++) {
                metadataDao.execute(totalDdl.get(position).getLeft());
            }
        } catch (Exception e) {
            LOG.error("Error executing custom sql: " + totalDdl.get(position));
            for (; position > 0; position--) {
                metadataDao.execute(totalDdl.get(position - 1).getRight());
            }
            throw new CrsException("Error executing custom sql", e);
        } finally {
            lock.unlock();
        }
    }
}
