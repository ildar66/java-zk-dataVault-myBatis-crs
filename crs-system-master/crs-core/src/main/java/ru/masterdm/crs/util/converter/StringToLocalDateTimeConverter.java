package ru.masterdm.crs.util.converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * {@link String} converts to {@link LocalDateTime}.
 * @author Evgeniy Melnikov
 */
@Component("stringToLocalDateTimeConverter")
public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {

    public static final String FORMAT = "dd.MM.yyyy HH:mm";

    @Override
    public LocalDateTime convert(String source) {
        return LocalDateTime.parse(source, DateTimeFormatter.ofPattern(FORMAT));
    }
}
