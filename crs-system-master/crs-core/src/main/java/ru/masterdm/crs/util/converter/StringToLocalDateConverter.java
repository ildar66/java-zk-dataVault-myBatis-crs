package ru.masterdm.crs.util.converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * {@link String} converts to {@link LocalDate}.
 * @author Evgeniy Melnikov
 */
@Component("stringToLocalDateConverter")
public class StringToLocalDateConverter implements Converter<String, LocalDate> {

    public static final String FORMAT = "dd.MM.yyyy";

    @Override
    public LocalDate convert(String source) {
        return LocalDate.parse(source, DateTimeFormatter.ofPattern(FORMAT));
    }
}
