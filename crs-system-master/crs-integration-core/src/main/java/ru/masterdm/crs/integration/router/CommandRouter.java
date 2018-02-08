package ru.masterdm.crs.integration.router;

import java.util.Map;

import ru.masterdm.crs.integration.ExecutionCommand;
import ru.masterdm.crs.integration.ExecutionCommandResult;
import ru.masterdm.crs.integration.ModuleMetadata;

/**
 * Performs command routing to corresponding queue.
 * @author Alexey Chalov
 */
public interface CommandRouter {

    /**
     * Performs {@link ExecutionCommand} routing to corresponding queue.
     * @param command {@link ExecutionCommand} instance
     */
    void route(ExecutionCommand command);

    /**
     * Performs {@link ExecutionCommand} routing to corresponding module.
     * @param command {@link ExecutionCommand} instance
     * @return execution result;
     */
    ExecutionCommandResult routeSync(ExecutionCommand command);

    /**
     * Returns map of registered modules.
     * @return map of registered modules
     */
    Map<String, ModuleMetadata> getModules();

    /**
     * Returns combined included modules property file names.
     * @return comma separated property file names
     */
    String[] getModulePropertyFileNames();
}