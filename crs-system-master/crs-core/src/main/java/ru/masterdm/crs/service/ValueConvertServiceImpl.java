package ru.masterdm.crs.service;

import static ru.masterdm.crs.exception.ConverterErrorCode.CANT_FIND_CONVERTER;
import static ru.masterdm.crs.exception.ConverterErrorCode.CONVERTER_CANT_CONVERT_VALUE;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.exception.ConverterException;
import ru.masterdm.crs.service.entity.AttributeFactory;

/**
 * Provide service to convert various input values into attribute proper datatype.
 * @author Pavel Masalov
 */
@Validated
@Service("valueConvertService")
public class ValueConvertServiceImpl implements ValueConvertService {

    @Autowired
    private ConversionService conversionService;

    @Override
    public Object convert(@NotNull AttributeMeta attributeMeta, Object sourceValue) {
        if (sourceValue == null)
            return null;

        Class clazz = AttributeFactory.getJavaClass(attributeMeta);
        if (clazz.equals(sourceValue.getClass()))
            return sourceValue;

        try {
            // use direct converter
            //if (clazz1 and clazz2) convertClass2ToClass1

            // or use predefined and custom spring converters
            return conversionService.convert(sourceValue, clazz);
        } catch (ConverterNotFoundException e) {
            throw new ConverterException(e, CANT_FIND_CONVERTER, attributeMeta, sourceValue);
        } catch (ConverterException e) {
            throw e;
        } catch (Exception e) {
            throw new ConverterException(e, CONVERTER_CANT_CONVERT_VALUE, attributeMeta, sourceValue);
        }
    }
}
