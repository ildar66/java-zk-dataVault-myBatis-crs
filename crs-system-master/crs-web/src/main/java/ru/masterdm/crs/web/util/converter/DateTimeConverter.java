package ru.masterdm.crs.web.util.converter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;

/**
 * Date time converter class.
 * @author Igor Matushak
 */
public class DateTimeConverter implements Converter {

    @Override
    public Object coerceToUi(Object beanProp, Component component, BindContext ctx) {
        LocalDateTime localDateTime = (LocalDateTime) beanProp;
        if (localDateTime != null) {
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

    @Override
    public Object coerceToBean(Object compAttr, Component component, BindContext ctx) {
        final Date date = (Date) compAttr;
        return (date == null) ? null
                              : Instant.ofEpochMilli(date.getTime())
                                       .atZone(ZoneId.systemDefault()).truncatedTo(ChronoUnit.MINUTES)
                                       .toLocalDateTime();
    }
}
