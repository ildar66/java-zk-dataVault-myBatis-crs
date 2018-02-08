package ru.masterdm.crs.web.service;

import org.zkoss.zul.ListModelList;

import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.web.domain.entity.EntityFilter;

/**
 * Criteria builder service.
 * @author Igor Matushak
 */
public interface CriteriaBuilderUiService {

    /**
     * Returns criteria by entity filters.
     * @param entityFilters entity filters
     * @param entityMetaService entity meta service
     * @return criteria by entity filters
     */
    Criteria getCriteriaByEntityFilters(ListModelList<EntityFilter> entityFilters, EntityMetaService entityMetaService);
}
