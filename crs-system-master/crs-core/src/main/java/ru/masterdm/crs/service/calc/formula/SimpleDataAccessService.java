package ru.masterdm.crs.service.calc.formula;

import java.time.LocalDateTime;
import java.util.List;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.entity.Entity;

/**
 * Functions to execute by javascript runned at nashorn.
 * @author Pavel Masalov
 */
public interface SimpleDataAccessService {

    /**
     * Read data from database.
     * @param modelActuality model actuality
     * @param dataActuality data actuality
     * @param calculation current calculation object
     * @param profile calculation profile
     * @param entityMetaKey entity metakey
     * @param paramPairs list of parameters, ex. [{"KEY": "DATAACC0102"}, {"CLASSIFIER01#NUMBER": 20}]
     * @return list of entities
     */
    List<?> lookupData(LocalDateTime modelActuality, LocalDateTime dataActuality, Calculation calculation, Entity profile, String entityMetaKey,
                       ScriptObjectMirror... paramPairs);
}
