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
import ru.masterdm.crs.service.entity.meta.ddl.CreateLinkSequenceDdlBuilder;

/**
 * Test for {@link CreateLinkSequenceDdlBuilder}.
 * @author Alexey Chalov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/crs-core-config.xml",
                                   "classpath:META-INF/spring/crs-datasource-config-test.xml"})
public class CreateLinkSequenceDdlBuilderTest {

    private static final Logger LOG = LoggerFactory.getLogger(CreateLinkSequenceDdlBuilderTest.class);

    private static EntityMeta entityMeta;

    @Autowired
    private CreateLinkSequenceDdlBuilder createLinkSequenceDdlBuilder;

    /**
     * Test for builder method.
     */
    @Test
    public void testBuild() {
        List<Pair<String, String>> scripts = createLinkSequenceDdlBuilder.build(entityMeta);
        for (Pair<String, String> script : scripts) {
            LOG.info(script.getLeft());
            LOG.info(script.getRight());
            Assert.assertNotNull(script.getLeft());
            Assert.assertNotNull(script.getRight());
        }
    }

    /**
     * Prepares initial test data.
     * @throws Exception exception
     */
    @BeforeClass
    public static void setup() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        entityMeta = objectMapper.readValue(CreateLinkSequenceDdlBuilderTest.class.getResourceAsStream("dv_api_test_entity_meta.json"),
                                            EntityMeta.class);
    }
}
