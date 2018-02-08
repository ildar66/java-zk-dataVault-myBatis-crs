package ru.masterdm.crs.test.util.aspect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ru.masterdm.crs.service.AspectTestService;

/**
 * Tests data validation aspect.
 * @author Sergey Valiev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/crs-core-config.xml",
                                   "classpath:META-INF/spring/crs-datasource-config-test.xml"})
public class DataValidationTest {

    @Autowired
    private AspectTestService aspectTestService;

    /**
     * Null check test.
     */
    @Test
    public void testNotNullCheck() {
        assertThat(catchThrowable(() -> aspectTestService.testMethod0())).isNull();

        assertThatThrownBy(() -> aspectTestService.testMethod1(null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(catchThrowable(() -> aspectTestService.testMethod1(1L))).isNull();

        assertThatThrownBy(() -> aspectTestService.testMethod2(null, null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(catchThrowable(() -> aspectTestService.testMethod2(null, 1L))).isNull();
    }
}
