package ru.masterdm.crs.util.aspect;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.PrimitiveIterator;
import java.util.stream.IntStream;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.masterdm.crs.dao.entity.meta.MetadataDao;
import ru.masterdm.crs.util.annotation.CurrentTimeStamp;

/**
 * Input data modification aspect.
 * @author Sergey Valiev
 */
@Component("inputDataModificationAspect")
public class InputDataModification {

    @Autowired
    private MetadataDao metadataDao;

    /**
     * Sets current time stamp to argument.
     * @param joinPoint join point
     * @return method's invocation result
     * @throws Throwable exception
     */
    public Object setCurrentTimeStamp(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = joinPoint.getTarget().getClass().getMethod(signature.getName(), signature.getParameterTypes());

        PrimitiveIterator.OfInt iterator = IntStream.range(0, method.getParameters().length)
                                                    .filter(i -> method.getParameters()[i].isAnnotationPresent(CurrentTimeStamp.class))
                                                    .filter(i -> method.getParameters()[i].getType() == LocalDateTime.class)
                                                    .filter(i -> joinPoint.getArgs()[i] == null)
                                                    .iterator();
        if (iterator.hasNext()) {
            LocalDateTime currentTimeStamp = metadataDao.getSysTimestamp();
            while (iterator.hasNext()) {
                joinPoint.getArgs()[iterator.nextInt()] = currentTimeStamp;
            }
        }

        return joinPoint.proceed(joinPoint.getArgs());
    }
}
