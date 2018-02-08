package ru.masterdm.crs.service;

import java.time.LocalDateTime;
import java.util.Date;

import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import ru.masterdm.crs.util.annotation.CurrentTimeStamp;

/**
 * Aspect test service.
 * @author Sergey Valiev
 */
@Validated
@Component
public class AspectTestService {

    /**
     * Test method.
     */
    public void testMethod0() {
    }

    /**
     * Test method.
     * @param p1 p1
     */
    public void testMethod1(@NotNull Long p1) {
    }

    /**
     * Test method.
     * @param p1 p1
     * @param p2 p1
     */
    public void testMethod2(Long p1, @NotNull Long p2) {
    }

    /**
     * Tests setting current time stamp.
     * @param p1 p1
     * @param ldt timestamp
     * @param p2 p2
     * @return timestamp
     */
    public LocalDateTime testCurrentTimeStamp1(Long p1, @CurrentTimeStamp LocalDateTime ldt, Long p2) {
        return ldt;
    }

    /**
     * Tests setting current time stamp.
     * @param ldt1 ldt1
     * @param ldt2 ldt2
     */
    public void testCurrentTimeStamp2(@CurrentTimeStamp LocalDateTime ldt1, @CurrentTimeStamp LocalDateTime ldt2) {
        if (ldt1 == null || ldt2 == null) {
            throw new RuntimeException();
        }

        if (!ldt1.equals(ldt2)) {
            throw new RuntimeException();
        }
    }

    /**
     * Tests setting current time stamp.
     * @param ldt timestamp
     * @return timestamp
     */
    public Date testCurrentTimeStamp3(@CurrentTimeStamp Date ldt) {
        return ldt;
    }
}
