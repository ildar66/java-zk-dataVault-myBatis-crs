package ru.masterdm.crs.web.domain;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

/**
 * Data slice class.
 * @author Igor Matushak
 */
@Component
@Scope(WebApplicationContext.SCOPE_SESSION)
public class DataSlice {

    private Map<String, LocalDateTime> dataSliceDatesMap;

    /**
     * Initiates.
     */
    @PostConstruct
    private void init() {
        dataSliceDatesMap = new HashMap<>();
    }

    /**
     * Returns data slice date by key.
     * @param key key
     * @return data slice date by key
     */
    public LocalDateTime getDataSliceDateByKey(String key) {
        return dataSliceDatesMap.get(key);
    }

    /**
     * Sets data slice date by key.
     * @param key key
     * @param dateSliceDate data slice date
     */
    public void setDataSliceDateByKey(String key, LocalDateTime dateSliceDate) {
        dataSliceDatesMap.put(key, dateSliceDate);
    }

    /**
     * Returns is data slice date set.
     * @param key key
     * @return is data slice date set
     */
    public boolean getDataSliceDateSet(String key) {
        return getDataSliceDateByKey(key) != null;
    }
}
