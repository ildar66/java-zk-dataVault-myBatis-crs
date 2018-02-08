package ru.masterdm.crs.test.service.entity.meta;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ru.masterdm.crs.domain.calc.Formula;
import ru.masterdm.crs.domain.calc.Model;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.service.EntityMetaService;

/**
 * Metadata tests for embedded objects.
 * @author Pavel Masalov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/crs-core-config.xml",
                                   "classpath:META-INF/spring/crs-datasource-config-test.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EmbeddedObjectMetadataTest {

    @Autowired
    private EntityMetaService entityMetaService;

    /**
     * Test calculation formula metadata.
     */
    @Test
    public void test01GetFormulaMetadata() {
        assertThat(entityMetaService.isEntityMetaExists(Formula.METADATA_KEY)).isTrue();
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKeyNoCache(Formula.METADATA_KEY, null);
        assertThat(entityMeta).isNotNull();
        assertThat(entityMeta.getAttributes().size()).isEqualTo(Formula.FormulaAttributeMeta.values().length);
    }

    /**
     * Test calculation model metadata.
     */
    @Test
    public void test02GetModelMetadata() {
        assertThat(entityMetaService.isEntityMetaExists(Model.METADATA_KEY)).isTrue();
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKeyNoCache(Model.METADATA_KEY, null);
        assertThat(entityMeta).isNotNull();
        assertThat(entityMeta.getAttributes().size()).isEqualTo(Model.ModelAttributeMeta.values().length);
    }
}
