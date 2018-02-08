package ru.masterdm.crs.integration.structure.imprt;

import org.springframework.stereotype.Service;

import ru.masterdm.crs.integration.ExecutionCommand;
import ru.masterdm.crs.integration.ExecutionCommandResult;
import ru.masterdm.crs.integration.router.SyncCommandProcessor;

/**
 * {@link SyncCommandProcessor} implementation for 'structure-import' module.
 * @author Alexey Chalov
 */
@Service(StructureImportSyncCommandProcessor.NAME)
public class StructureImportSyncCommandProcessor implements SyncCommandProcessor {

    public static final String NAME = "structureImportSyncCommandProcessor";

    @Override
    public ExecutionCommandResult execute(ExecutionCommand command) {
        ExecutionCommandResult result = new ExecutionCommandResult();
        result.setResult("structure-import: not implemented yet");
        return result;
    }
}
