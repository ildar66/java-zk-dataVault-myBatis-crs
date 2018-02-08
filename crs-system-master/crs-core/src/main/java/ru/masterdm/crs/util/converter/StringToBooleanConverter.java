package ru.masterdm.crs.util.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * {@link String} converts to {@link Boolean}.
 * @author Evgeniy Melnikov
 */
@Component("stringToBooleanConverter")
public class StringToBooleanConverter implements Converter<String, Boolean> {

    @Override
    public Boolean convert(String source) {
        return source.equalsIgnoreCase("1")
               || source.equalsIgnoreCase("true")
               ? Boolean.TRUE
               : Boolean.FALSE;
    }
}
