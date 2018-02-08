package ru.masterdm.crs.test.service.entity.meta;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.service.entity.meta.DvStructureManagement;

/**
 * Test for {@link DvStructureManagement}.
 * @author Alexey Chalov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ContextConfiguration(locations = {"classpath:META-INF/spring/crs-core-config.xml",
                                   "classpath:META-INF/spring/crs-datasource-config-test.xml"})
public class DvStructureManagementTest {

    private static EntityMeta entityMeta;
    private static EntityMeta entityMetaOld;
    private static EntityMeta entityMetaNew;

    @Autowired
    private DvStructureManagement dvStructureManagement;

    /**
     * Test method for {@link DvStructureManagement#create(EntityMeta)}.
     */
    @Test
    public void testCreate() {
        List<AttributeMeta> attributes = new ArrayList<AttributeMeta>();
        entityMeta.setAttributes(attributes);
        dvStructureManagement.create(entityMeta);
    }

    /**
     * Test method for {@link DvStructureManagement#update(EntityMeta, EntityMeta)}.
     */
    @Test
    public void testUpdate() {
        dvStructureManagement.update(entityMetaOld, entityMetaNew);
    }

    /**
     * Prepares initial test data.
     * @throws Exception exception
     */
    @BeforeClass
    public static void setup() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        entityMeta = objectMapper.readValue(
                DvStructureManagementTest.class.getResourceAsStream("dv_test_entity_meta.json"), EntityMeta.class
        );
        entityMetaOld = objectMapper.readValue(
                DvStructureManagementTest.class.getResourceAsStream("dv_test_entity_meta_old.json"), EntityMeta.class
        );
        entityMetaNew = objectMapper.readValue(
                DvStructureManagementTest.class.getResourceAsStream("dv_test_entity_meta_new.json"), EntityMeta.class
        );
    }
}
