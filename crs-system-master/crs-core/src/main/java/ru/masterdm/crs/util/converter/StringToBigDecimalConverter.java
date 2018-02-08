package ru.masterdm.crs.util.converter;

import java.math.BigDecimal;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * {@link String} converts to {@link BigDecimal}.
 * @author Pavel Masalov
 */
@Component("stringToBigDecimalConverter")
public class StringToBigDecimalConverter implements Converter<String, BigDecimal> {

    @Override
    public BigDecimal convert(String source) {
        // TODO very simple assumption about input formats, will use more
        if (source.equals(""))
            return null;
        return new BigDecimal(source.replaceAll(",", "."));
    }
}
