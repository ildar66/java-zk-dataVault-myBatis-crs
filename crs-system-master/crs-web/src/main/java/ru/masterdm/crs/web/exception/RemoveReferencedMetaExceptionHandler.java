package ru.masterdm.crs.web.exception;

import java.text.MessageFormat;
import java.util.StringJoiner;

import org.zkoss.util.resource.Labels;

import ru.masterdm.crs.exception.meta.RemoveReferencedMetaException;
import ru.masterdm.crs.web.domain.UserProfile;

/**
 * Remove referenced meta exception handler.
 * @author Igor Matushak
 */
public class RemoveReferencedMetaExceptionHandler implements ExceptionHandler<RemoveReferencedMetaException> {

    @Override
    public ExceptionEnvelope getExceptionEnvelope(RemoveReferencedMetaException e, UserProfile userProfile) {
        StringJoiner refEntityMetas = new StringJoiner("\n");
        e.getReferenceByEntityMetas().forEach(i -> refEntityMetas.add(String.format("%s (%s)",
                                                                                    i.getName().getDescription(userProfile.getLocale()),
                                                                                    i.getKey())));

        String refEntityNameMessage = String.format("%s (%s)",
                                                    e.getReferencedEntityMeta().getName().getDescription(userProfile.getLocale()),
                                                    e.getReferencedEntityMeta().getKey());
        String errorMessage = MessageFormat.format(Labels.getLabel("exception_remove_referenced_meta_message"), refEntityNameMessage,
                                                   refEntityNameMessage, "\n" + refEntityMetas.toString());
        return new ExceptionEnvelope(errorMessage, true);
    }
}
