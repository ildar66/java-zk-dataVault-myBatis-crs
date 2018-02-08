package ru.masterdm.crs.util.json;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Custom object mapper with {@link LocalDateTime} serialization support.
 * @author Alexey Chalov
 */
public class CrsObjectMapper extends ObjectMapper {

    /**
     * Constructor.
     */
    public CrsObjectMapper() {
        findAndRegisterModules();
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, false);
        configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
    }
}
