package ru.masterdm.crs.test.service.entity.meta.ddl;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.service.entity.meta.ddl.CreateHubSelfRefLinkDdlBuilder;

/**
 * Test for {@link CreateHubSelfRefLinkDdlBuilder}.
 * @author Alexey Chalov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/crs-core-config.xml",
                                   "classpath:META-INF/spring/crs-datasource-config-test.xml"})
public class CreateHubSelfRefLinkDdlBuilderTest {

    private static final Logger LOG = LoggerFactory.getLogger(CreateHubSelfRefLinkDdlBuilderTest.class);

    private static EntityMeta entityMeta;

    @Autowired
    private CreateHubSelfRefLinkDdlBuilder createHubSelfRefLinkDdlBuilder;

    /**
     * Test for builder method.
     */
    @SuppressWarnings("Duplicates")
    @Test
    public void testBuild() {
        List<Pair<String, String>> scripts = createHubSelfRefLinkDdlBuilder.build(entityMeta);
        for (Pair<String, String> sc : scripts) {
            LOG.info(sc.getLeft());
            LOG.info(sc.getRight());
            Assert.assertNotNull(sc.getLeft());
            Assert.assertNotNull(sc.getRight());
        }
    }

    /**
     * Prepares initial test data.
     * @throws Exception exception
     */
    @BeforeClass
    public static void setup() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        entityMeta = objectMapper.readValue(CreateHubSelfRefLinkDdlBuilderTest.class.getResourceAsStream("dv_api_test_entity_meta.json"),
                                            EntityMeta.class);
    }
}
