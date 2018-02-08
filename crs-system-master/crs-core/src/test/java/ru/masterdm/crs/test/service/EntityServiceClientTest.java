package ru.masterdm.crs.test.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.meta.ClientInnAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;

/**
 * CONTRACTOR predefined entity test.
 * @author Pavel Masalov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/crs-core-config.xml",
                                   "classpath:META-INF/spring/crs-datasource-config-test.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EntityServiceClientTest {

    @Autowired
    private EntityService entityService;
    @Autowired
    private EntityMetaService entityMetaService;

    /**
     * Create or load entity.
     * @param entityMeta entity metadata
     * @param key key
     * @param ldts load datetime
     * @return entity loaded or empty
     */
    private Entity getOrCreateEmpty(EntityMeta entityMeta, String key, LocalDateTime ldts) {
        Entity entity;
        if (entityService.getEntityIdByKey(entityMeta, key) == null)
            entity = entityService.newEmptyEntity(entityMeta);
        else
            entity = entityService.getEntity(entityMeta, key, ldts);

        assertThat(entity).isNotNull();
        entity.setKey(key);
        return entity;
    }

    /**
     * Simple select persists CONTRACTOR test.
     */
    @Test
    public void test001ClientPersist() {
        String key = "C0";
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(ClientInnAttributeMeta.METADATA_KEY, null);
        Entity entity = getOrCreateEmpty(entityMeta, key, null);
        entity.setAttributeValue(ClientInnAttributeMeta.TAX_ID.getKey(), key);
        entityService.persistEntity(entity);

        Entity check = entityService.getEntity(entityMeta, key, null);
        assertThat(check).isNotNull();
        assertThat(check.getAttributeValue(ClientInnAttributeMeta.TAX_ID.getKey())).isEqualTo(key);
    }
}
