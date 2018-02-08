package ru.masterdm.crs.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Service;

import ru.masterdm.crs.dao.SettingDao;
import ru.masterdm.crs.dao.SettingDao.SettingMnemo;
import ru.masterdm.crs.domain.MultilangDescription;
import ru.masterdm.crs.domain.entity.Department;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.meta.AttributeLocale;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.integration.CpiDepartment;
import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.exception.integration.IntegrationErrorCode;
import ru.masterdm.crs.exception.integration.IntegrationException;
import ru.masterdm.crs.integration.ExecutionCommand;
import ru.masterdm.crs.integration.ExecutionCommandName;
import ru.masterdm.crs.integration.ExecutionCommandResult;
import ru.masterdm.crs.integration.IntegrationModuleName;
import ru.masterdm.crs.integration.ModuleMetadata;

/**
 * {@link IntegrationService} implementation.
 * @author Alexey Chalov
 */
@Service("integrationService")
public class IntegrationServiceImpl implements IntegrationService {

    @Autowired
    private SettingDao settingDao;
    @Autowired
    private EntityService entityService;
    @Autowired
    private EntityMetaService entityMetaService;

    @EndpointInject(context = "integrationContext", uri = "direct:integrationSyncRoute")
    private ProducerTemplate producerTemplate;

    @Value("#{config['integration.module.url']}")
    private String defaultModuleUrl;
    @Value("#{config['integration.services.context.path']}")
    private String servicesContextPath;

    private DefaultConversionService conversionService;

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationServiceImpl.class);

    @Override
    @SuppressWarnings("unchecked")
    public Collection<ModuleMetadata> getModules() {
        try {
            return (Collection<ModuleMetadata>) producerTemplate.requestBodyAndHeaders(null, prepareHeaders("modules/list", "GET"));
        } catch (Exception e) {
            throw new IntegrationException(e.getMessage(), e, IntegrationErrorCode.INTEGRATION_SYSTEM_UNAVAILABLE);
        }
    }

    @Override
    public LocalDateTime getCpiLatestSyncDate() {
        checkModule(IntegrationModuleName.CLIENT_PORTAL);
        ExecutionCommand command = new ExecutionCommand();
        command.setModule(IntegrationModuleName.CLIENT_PORTAL.getModuleName());
        command.setOperation(ExecutionCommandName.CPI_LATEST_SYNC_DATE.getCommandName());
        ExecutionCommandResult result = executeSync(command);
        if (result.getException() != null) {
            throw new IntegrationException(
                    result.getException(), IntegrationErrorCode.EXECUTION_EXCEPTION, IntegrationModuleName.CLIENT_PORTAL.getModuleName()
            );
        }
        boolean empty = result.getResult() == null || String.valueOf(result.getResult()).isEmpty();
        return !empty ? LocalDateTime.parse(String.valueOf(result.getResult())) : null;
    }

    @Override
    public List<CpiDepartment> getCpiDepartments(String namePattern, AttributeLocale locale) {
        checkModule(IntegrationModuleName.CLIENT_PORTAL);
        ExecutionCommand command = new ExecutionCommand();
        command.setModule(IntegrationModuleName.CLIENT_PORTAL.getModuleName());
        command.setOperation(ExecutionCommandName.CPI_DEPARMENTS_BY_FILTER.getCommandName());
        if (namePattern != null && !namePattern.trim().isEmpty() && locale != null) {
            Map<String, String> params = new HashMap<>();
            params.put("department_name_pattern", namePattern);
            params.put("locale", locale.name());
            command.setContext(params);
        }
        return getCpiDepartments(executeSync(command));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Pair<Department, List<CpiDepartment>>> getCpiDepartmentMappings() {
        checkModule(IntegrationModuleName.CLIENT_PORTAL);
        ExecutionCommand command = new ExecutionCommand();
        command.setModule(IntegrationModuleName.CLIENT_PORTAL.getModuleName());
        command.setOperation(ExecutionCommandName.CPI_DEPARTMENT_MAPPINGS.getCommandName());
        ExecutionCommandResult result = executeSync(command);
        List<Pair<Long, Long>> departmentMappings = (List<Pair<Long, Long>>) ((List) result.getResult()).stream().map(r -> {
            return Pair.of(
                    Long.parseLong((String) ((Map) r).keySet().iterator().next()),
                    ((Integer) ((Map) r).values().iterator().next()).longValue()
            );
        }).collect(Collectors.toList());

        /* read CRS departments */
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(Department.METADATA_KEY, null);
        Criteria criteria = new Criteria();
        criteria.setHubIds(departmentMappings.stream().map(pair -> pair.getLeft()).collect(Collectors.toList()));
        Map<Long, Department> availableDepartments =
                ((List<Department>) entityService.getEntities(entityMeta, criteria, null, null)).stream()
                                                                                                .collect(Collectors.toMap(dep -> dep.getHubId(),
                                                                                                                          dep -> dep));

        /* read CPI departments */
        command.setOperation(ExecutionCommandName.CPI_DEPARMENTS_BY_FILTER.getCommandName());
        Map<String, Object> params = new HashMap<>();
        params.put("department_ids", departmentMappings.stream().map(r -> r.getRight()).collect(Collectors.toList()));
        command.setContext(params);

        result = executeSync(command);
        Map<Long, CpiDepartment> availableCpiDepartments = getCpiDepartments(result).stream().collect(Collectors.toMap(d -> d.getId(), d -> d));

        /* finally build the result */
        Map<Long, List<Long>> combinedDepartmentMappings = departmentMappings.stream().collect(HashMap::new, (map, p) -> {
            List<Long> cpiDepIds = (List<Long>) map.get(p.getLeft());
            if (cpiDepIds == null) {
                cpiDepIds = new ArrayList<>();
                map.put(p.getLeft(), cpiDepIds);
            }
            cpiDepIds.add(p.getRight());
        }, Map::putAll);

        return combinedDepartmentMappings.keySet().stream().map(k ->
                                                                        Pair.of(
                                                                                availableDepartments.get(k),
                                                                                combinedDepartmentMappings.get(k).stream()
                                                                                                          .map(v -> availableCpiDepartments.get(v))
                                                                                                          .collect(Collectors.toList())
                                                                        )
        ).collect(Collectors.toList());
    }

    /**
     * Extracts CPI department list from execution command result.
     * @param result {@link ExecutionCommandResult} instance
     * @return list of {@link CpiDepartment} instances
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<CpiDepartment> getCpiDepartments(ExecutionCommandResult result) {
        List<CpiDepartment> cpiDepartments = (List<CpiDepartment>) ((List) result.getResult()).stream().map(r -> {
            CpiDepartment dto = new CpiDepartment();
            try {
                BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(dto);
                wrapper.setConversionService(conversionService);
                wrapper.setAutoGrowNestedPaths(true);
                wrapper.setPropertyValues(new MutablePropertyValues((Map) r), true, true);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                throw new CrsException("Cannot populate bean fields from passed map: " + r, e);
            }
            return dto;
        }).collect(Collectors.toList());
        return cpiDepartments;
    }

    @Override
    public void persistDepartmentMappings(Department crsDepartment, List<CpiDepartment> cpiDepartments) {
        checkModule(IntegrationModuleName.CLIENT_PORTAL);
        ExecutionCommand command = new ExecutionCommand();
        command.setModule(IntegrationModuleName.CLIENT_PORTAL.getModuleName());
        command.setOperation(ExecutionCommandName.CPI_PERSIST_DEPARTMENT_MAPPINGS.getCommandName());

        Map<String, Object> params = new HashMap<>();
        params.put("crs_department_id", crsDepartment.getHubId());
        params.put("cpi_department_ids", cpiDepartments.stream().map(d -> d.getId()).collect(Collectors.toList()));
        command.setContext(params);
        producerTemplate.requestBodyAndHeaders(command, prepareHeaders("executesync", "POST"));
    }

    @Override
    public boolean isModuleAvailable(String moduleName) {
        return getModules().stream().filter(m -> m.getName().equals(moduleName)).count() == 1;
    }

    /**
     * Initializes service.
     */
    @PostConstruct
    void initialize() {
        conversionService = (DefaultConversionService) DefaultConversionService.getSharedInstance();
        conversionService.addConverter(new Converter<Map<?, ?>, MultilangDescription>() {

            @Override
            public MultilangDescription convert(Map<?, ?> source) {
                MultilangDescription desc = new MultilangDescription();
                BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(desc);
                bw.setPropertyValues(new MutablePropertyValues(source), true, true);
                return desc;
            }
        });
    }

    /**
     * Checks, whether integration module exists and registered:
     * throws {@link NoSuchIntegrationModuleException} if module is absent.
     * @param moduleName module name
     */
    private void checkModule(IntegrationModuleName moduleName) {
        if (!isModuleAvailable(moduleName.getModuleName())) {
            throw new IntegrationException(IntegrationErrorCode.NO_SUCH_INTEGRATION_MODULE, moduleName.getModuleName());
        }
    }

    /**
     * Returns map of headers containing REST operation URL string and operation switch key.
     * @param operationPath operation path
     * @param httpMethod HTTP method
     * @return map of headers
     */
    private Map<String, Object> prepareHeaders(String operationPath, String httpMethod) {
        String moduleUrl = settingDao.getSettingValue(SettingMnemo.INTEGRATION_MODULE_URL);
        if (moduleUrl == null) {
            moduleUrl = defaultModuleUrl;
        }
        if (!moduleUrl.endsWith("/")) {
            moduleUrl = moduleUrl + "/";
        }
        Map<String, Object> headers = new HashMap<>();
        headers.put(Exchange.HTTP_URI, moduleUrl + servicesContextPath + operationPath);
        headers.put("operation", operationPath);
        headers.put(Exchange.HTTP_METHOD, httpMethod);
        return headers;
    }

    /**
     * Performs synchronous command execution.
     * @param command {@link ExecutionCommand} instance
     * @return {@link ExecutionCommandResult} instance
     */
    private ExecutionCommandResult executeSync(ExecutionCommand command) {
        return (ExecutionCommandResult) producerTemplate.requestBodyAndHeaders(command, prepareHeaders("executesync", "POST"));
    }
}
