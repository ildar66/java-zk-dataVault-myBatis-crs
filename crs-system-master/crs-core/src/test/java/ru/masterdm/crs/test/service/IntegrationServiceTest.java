package ru.masterdm.crs.test.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ru.masterdm.crs.domain.entity.Department;
import ru.masterdm.crs.domain.entity.meta.AttributeLocale;
import ru.masterdm.crs.domain.integration.CpiDepartment;
import ru.masterdm.crs.service.IntegrationService;

/**
 * Test collection for {@link IntegrationService} methods.
 * @author Alexey Chalov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:META-INF/spring/crs-core-config.xml",
        "classpath:META-INF/spring/crs-integration-client.xml",
        "classpath:META-INF/spring/crs-datasource-config-test.xml"
})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Ignore
public class IntegrationServiceTest {

    @Autowired
    private IntegrationService integrationService;

    /**
     * Test for {@link IntegrationService#getModules()}.
     */
    @Test
    public void test01GetModules() {
        assertThat(integrationService.getModules().size()).isGreaterThan(0);
    }

    /**
     * Test for {@link IntegrationService#getCpiLatestSyncDate()}.
     */
    @Test
    public void test02GetCpiLatestSyncDate() {
        assertThat(catchThrowable(() -> integrationService.getCpiLatestSyncDate())).isNull();
    }

    /**
     * Test for {@link IntegrationService#getCpiDepartments(String, Locale)}.
     */
    @Test
    public void test03GetCpiDepartments() {
        List<CpiDepartment> cpiDepartments = integrationService.getCpiDepartments("петербург", AttributeLocale.RU);
        assertThat(cpiDepartments.size()).isEqualTo(1);
        assertThat(String.valueOf(cpiDepartments.get(0).getId())).isEqualTo("555320");
        assertThat(cpiDepartments.get(0).getName().getDescriptionEn().equals("Saint Petersburg branch"));
        assertThat(cpiDepartments.get(0).getName().getDescriptionRu().equals("Филиал в г. Санкт-Петербурге"));
        assertThat(cpiDepartments.get(0).getFullName().getDescriptionEn().equals("Petersburg branch"));
        assertThat(cpiDepartments.get(0).getFullName().getDescriptionRu().equals("Филиал в г. Санкт-Петербурге"));
    }

    /**
     * Composite test for {@link IntegrationService#persistDepartmentMappings(Long, List)}
     * and {@link IntegrationService#getCpiDepartmentMappings()} methods.
     */
    @Test
    public void test04PersistAndGetDepartmentMappings() {
        Department crsDepartment = new Department();
        crsDepartment.setId(1L);
        CpiDepartment cpiDepartment1 = new CpiDepartment();
        cpiDepartment1.setId(1L);
        CpiDepartment cpiDepartment2 = new CpiDepartment();
        cpiDepartment2.setId(2L);

        integrationService.persistDepartmentMappings(crsDepartment, Collections.emptyList());
        List<Pair<Department, List<CpiDepartment>>> mappings = integrationService.getCpiDepartmentMappings();
        assertThat(mappings).isEmpty();

        integrationService.persistDepartmentMappings(crsDepartment, Arrays.asList(cpiDepartment1, cpiDepartment2));
        mappings = integrationService.getCpiDepartmentMappings();
        assertThat(mappings.size()).isEqualTo(1);
        assertThat(mappings.get(0).getRight().size()).isEqualTo(2);

        integrationService.persistDepartmentMappings(crsDepartment, Arrays.asList(cpiDepartment1));
        mappings = integrationService.getCpiDepartmentMappings();
        assertThat(mappings.size()).isEqualTo(1);
        assertThat(mappings.get(0).getRight().size()).isEqualTo(1);
    }
}
