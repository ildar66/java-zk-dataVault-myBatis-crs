package ru.masterdm.crs.integration.structure.export;

import org.springframework.stereotype.Service;

import ru.masterdm.crs.integration.ExecutionCommand;
import ru.masterdm.crs.integration.ExecutionCommandResult;
import ru.masterdm.crs.integration.router.SyncCommandProcessor;

/**
 * {@link SyncCommandProcessor} implementation for 'structure-export' module.
 * @author Alexey Chalov
 */
@Service(StructureExportSyncCommandProcessor.NAME)
public class StructureExportSyncCommandProcessor implements SyncCommandProcessor {

    public static final String NAME = "structureExportSyncCommandProcessor";

    @Override
    public ExecutionCommandResult execute(ExecutionCommand command) {
        ExecutionCommandResult result = new ExecutionCommandResult();
        result.setResult("structure-export: not implemented yet");
        return result;
    }
}
