package ru.masterdm.crs.integration.rating.imp;

import org.springframework.stereotype.Service;

import ru.masterdm.crs.integration.ExecutionCommand;
import ru.masterdm.crs.integration.ExecutionCommandResult;
import ru.masterdm.crs.integration.router.SyncCommandProcessor;

/**
 * {@link SyncCommandProcessor} implementation for 'rating-import' module.
 * @author Alexey Chalov
 */
@Service(RatingImportSyncCommandProcessor.NAME)
public class RatingImportSyncCommandProcessor implements SyncCommandProcessor {

    public static final String NAME = "ratingImportSyncCommandProcessor";

    @Override
    public ExecutionCommandResult execute(ExecutionCommand command) {
        ExecutionCommandResult result = new ExecutionCommandResult();
        result.setResult("rating-import: not implemented yet");
        return result;
    }
}
