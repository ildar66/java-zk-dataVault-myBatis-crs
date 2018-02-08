package ru.masterdm.crs.integration.router;

import ru.masterdm.crs.integration.ExecutionCommand;
import ru.masterdm.crs.integration.ExecutionCommandResult;

/**
 * Synchronous command processor interface.
 * @author Alexey Chalov
 */
public interface SyncCommandProcessor {

    /**
     * Executes command and returns execution result.
     * @param command {@link ExecutionCommand} instance
     * @return execution result
     */
    ExecutionCommandResult execute(ExecutionCommand command);
}
