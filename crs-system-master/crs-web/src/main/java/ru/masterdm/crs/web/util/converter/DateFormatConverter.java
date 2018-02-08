package ru.masterdm.crs.web.util.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;

/**
 * Date format converter class.
 * @author Igor Matushak
 */
public class DateFormatConverter implements Converter {

    @Override
    public Object coerceToUi(Object beanProp, Component component, BindContext ctx) {
        final String format = Labels.getLabel("date_format");
        if (format == null) {
            throw new IllegalArgumentException("format attribute not found");
        }
        LocalDate localDate = (LocalDate) beanProp;
        Date date = null;
        if (localDate != null) {
            date = Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        }
        return date == null ? null : new SimpleDateFormat(format).format(date);
    }

    @Override
    public Object coerceToBean(Object compAttr, Component component, BindContext ctx) {
        final String format = Labels.getLabel("date_format");
        if (format == null) {
            throw new IllegalArgumentException("format attribute not found");
        }
        final String date = (String) compAttr;
        try {
            return (date == null) ? null
                                  : Instant.ofEpochMilli(new SimpleDateFormat(format).parse(date).getTime())
                                           .atZone(ZoneId.systemDefault())
                                           .toLocalDate();
        } catch (ParseException e) {
            throw UiException.Aide.wrap(e);
        }
    }
}
