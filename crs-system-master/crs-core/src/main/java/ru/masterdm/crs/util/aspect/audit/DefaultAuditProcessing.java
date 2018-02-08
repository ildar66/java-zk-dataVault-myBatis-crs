package ru.masterdm.crs.util.aspect.audit;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ru.masterdm.crs.domain.AuditAction;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.util.annotation.Audit;

/**
 * Default audit processor.
 * @author Kuzmin Mikhail
 * @author Alexey Chalov
 */
@Component("defaultAuditProcessingAspect")
public class DefaultAuditProcessing extends AbstractAuditProcessing {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAuditProcessing.class);

    @Override
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        Audit an = getAuditAnnotation(joinPoint);

        if (!StringUtils.isEmpty(an.action().customLogic())) {
            AuditProcessing processor = (AuditProcessing) applicationContext.getBean(an.action().customLogic());
            return processor.log(joinPoint);
        } else {
            return innerLog(joinPoint, an.action());
        }
    }

    /**
     * Performs intercepted method call, writes logs.
     * @param joinPoint {@link JoinPoint} instance
     * @param action {@link AuditAction} constant
     * @return intercepted method invocation result
     * @throws Throwable if intercepted method threw exception or log writing failed
     */
    private Object innerLog(ProceedingJoinPoint joinPoint, AuditAction action) throws Throwable {
        try {
            Object result = joinPoint.proceed(joinPoint.getArgs());
            Arrays.stream(joinPoint.getArgs())
                  .filter(arg -> (arg instanceof Entity))
                  .forEach(arg -> writeLog((Entity) arg, action));
            return result;
        } catch (Throwable e) {
            LOG.error("Error caught in default audit processor", e);
            throw e;
        }
    }

    /**
     * Returns audit annotation for parameters.
     * @param joinPoint proceeding join point
     * @return Audit annotation object
     * @throws NoSuchMethodException exception
     */
    private Audit getAuditAnnotation(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = joinPoint.getTarget().getClass().getMethod(signature.getName(), signature.getParameterTypes());
        return method.getAnnotation(Audit.class);
    }
}
