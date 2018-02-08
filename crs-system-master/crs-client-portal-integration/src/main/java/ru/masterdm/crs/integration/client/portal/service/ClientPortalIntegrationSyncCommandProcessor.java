package ru.masterdm.crs.integration.client.portal.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.masterdm.crs.integration.ExecutionCommand;
import ru.masterdm.crs.integration.ExecutionCommandName;
import ru.masterdm.crs.integration.ExecutionCommandResult;
import ru.masterdm.crs.integration.client.portal.dao.ClientPortalIntegrationDao;
import ru.masterdm.crs.integration.router.SyncCommandProcessor;

/**
 * {@link SyncCommandProcessor} implementation for 'client-portal-integration' module.
 * @author Alexey Chalov
 */
@Service(ClientPortalIntegrationSyncCommandProcessor.NAME)
public class ClientPortalIntegrationSyncCommandProcessor implements SyncCommandProcessor {

    @Autowired
    private ClientPortalIntegrationDao clientPortalIntegrationDao;

    public static final String NAME = "clientPortalIntegrationSyncCommandProcessor";
    private static final Logger LOG = LoggerFactory.getLogger(ClientPortalIntegrationSyncCommandProcessor.class);

    @Override
    @SuppressWarnings("unchecked")
    public ExecutionCommandResult execute(ExecutionCommand command) {
        ExecutionCommandResult result = new ExecutionCommandResult();
        ExecutionCommandName commandName =
            Arrays.stream(ExecutionCommandName.values())
                  .filter(cn -> cn.getCommandName().equals(command.getOperation())).findFirst().get();
        try {
            Map<String, Object> params = (Map<String, Object>) command.getContext();
            switch (commandName) {
                case CPI_LATEST_SYNC_DATE:
                        result.setResult(clientPortalIntegrationDao.getLatestSyncDate());
                    break;
                case CPI_DEPARMENTS_BY_FILTER:
                    String namePattern = params != null ? String.valueOf(params.get("department_name_pattern")) : null;
                    String locale = params != null ? String.valueOf(params.get("locale")) : null;
                    List<Long> departmentIds = params != null && params.get("department_ids") != null
                                             ? (List<Long>) params.get("department_ids") : null;
                    result.setResult(clientPortalIntegrationDao.getDepartments(namePattern, locale, departmentIds));
                    break;
                case CPI_DEPARTMENT_MAPPINGS:
                    result.setResult(clientPortalIntegrationDao.getDepartmentMappings());
                    break;
                case CPI_PERSIST_DEPARTMENT_MAPPINGS:
                    clientPortalIntegrationDao.persistDepartmentMappings(
                        Long.parseLong(String.valueOf(params.get("crs_department_id"))),
                        (List<Long>) params.get("cpi_department_ids")
                    );
                    break;
                default:
                    throw new IllegalArgumentException("No handler for operation " + command.getOperation() + "' provided.");
            }
        } catch (Throwable e) {
            LOG.error(e.getMessage(), e);
            result.setException(ExceptionUtils.getStackTrace(e));
        }
        return result;
    }
}
