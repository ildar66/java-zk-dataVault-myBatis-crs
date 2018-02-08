package ru.masterdm.crs.test.util.aspect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.LocalDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ru.masterdm.crs.service.AspectTestService;

/**
 * Tests input data modification aspect.
 * @author Sergey Valiev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/crs-core-config.xml",
                                   "classpath:META-INF/spring/crs-datasource-config-test.xml"})
public class InputDataModificationTest {

    @Autowired
    private AspectTestService aspectTestService;

    /**
     * Set current timestamp test.
     */
    @Test
    public void testSetCurrentTimeStamp() {
        LocalDateTime localDateTime = LocalDateTime.now();
        assertEquals(localDateTime, aspectTestService.testCurrentTimeStamp1(null, localDateTime, null));
        assertNotNull(aspectTestService.testCurrentTimeStamp1(null, null, null));
        assertThat(catchThrowable(() -> aspectTestService.testCurrentTimeStamp2(null, null))).isNull();
        assertNull(aspectTestService.testCurrentTimeStamp3(null));
    }
}
