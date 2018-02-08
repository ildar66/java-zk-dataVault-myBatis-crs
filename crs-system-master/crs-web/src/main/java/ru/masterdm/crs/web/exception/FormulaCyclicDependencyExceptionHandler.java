package ru.masterdm.crs.web.exception;

import java.util.StringJoiner;

import org.zkoss.util.resource.Labels;

import ru.masterdm.crs.exception.calc.FormulaCyclicDependencyException;
import ru.masterdm.crs.web.domain.UserProfile;

/**
 * Formula cyclic dependency exception handler.
 * @author Alexey Kirilchev
 */
public class FormulaCyclicDependencyExceptionHandler implements ExceptionHandler<FormulaCyclicDependencyException> {

    @Override
    public ExceptionEnvelope getExceptionEnvelope(FormulaCyclicDependencyException e, UserProfile userProfile) {
        StringJoiner cyclicMsg = new StringJoiner("\n");
        e.getFormulas().forEach(i -> cyclicMsg.add(String.format("%s (%s)", i.getName().getDescription(userProfile.getLocale()), i.getKey())));
        String errorMessage = Labels.getLabel("exception_formula_cyclic_dependency_message") + ":\n" + cyclicMsg.toString();
        return new ExceptionEnvelope(errorMessage, true);
    }
}
