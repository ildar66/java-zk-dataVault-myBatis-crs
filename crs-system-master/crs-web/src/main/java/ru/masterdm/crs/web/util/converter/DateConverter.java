package ru.masterdm.crs.web.util.converter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;

/**
 * Date converter class.
 * @author Igor Matushak
 */
public class DateConverter implements Converter {

    @Override
    public Object coerceToUi(Object beanProp, Component component, BindContext ctx) {
        LocalDate localDate = (LocalDate) beanProp;
        if (localDate != null) {
            return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

    @Override
    public Object coerceToBean(Object compAttr, Component component, BindContext ctx) {
        final Date date = (Date) compAttr;
        return (date == null) ? null
                              : Instant.ofEpochMilli(date.getTime())
                                       .atZone(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS)
                                       .toLocalDate();
    }
}
