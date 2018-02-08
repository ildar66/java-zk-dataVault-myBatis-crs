package ru.masterdm.crs.util.converter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Convert {@link Date} to {@link LocalDate}.
 * @author Pavel Masalov
 */
@Component("dateToLocalDateConverter")
public class DateToLocalDateConverter implements Converter<Date, LocalDate> {

    @Override
    public LocalDate convert(Date source) {
        return source.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
