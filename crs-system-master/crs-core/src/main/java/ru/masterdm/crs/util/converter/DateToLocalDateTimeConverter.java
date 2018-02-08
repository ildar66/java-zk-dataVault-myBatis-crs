package ru.masterdm.crs.util.converter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Convert {@link Date} to {@link LocalDateTime}.
 * @author Pavel Masalov
 */
@Component("dateToLocalDateTimeConverter")
public class DateToLocalDateTimeConverter implements Converter<Date, LocalDateTime> {

    @Override
    public LocalDateTime convert(Date source) {
        return source.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
