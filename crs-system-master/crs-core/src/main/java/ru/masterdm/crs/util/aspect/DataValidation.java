package ru.masterdm.crs.util.aspect;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.IntStream;

import javax.validation.constraints.NotNull;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * Data validation aspect.
 * @author Sergey Valiev
 */
@Component("dataValidationAspect")
public class DataValidation {

    /**
     * Null check processing.
     * @param joinPoint join point
     * @throws NoSuchMethodException exception
     */
    public void processNullCheck(JoinPoint joinPoint) throws NoSuchMethodException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = joinPoint.getTarget().getClass().getMethod(signature.getName(), signature.getParameterTypes());
        final String errorTemplate = "method '%s.%s' has null param. Type: '%s'. Index: '%d'";

        IntStream.range(0, method.getParameters().length)
                 .filter(i -> method.getParameters()[i].isAnnotationPresent(NotNull.class))
                 .forEach(i -> Optional.ofNullable(joinPoint.getArgs()[i])
                                       .orElseThrow(() -> new IllegalArgumentException(String.format(errorTemplate,
                                                                                                     method.getDeclaringClass().getName(),
                                                                                                     method.getName(),
                                                                                                     method.getParameters()[i].getType().getName(),
                                                                                                     i))));
    }
}
