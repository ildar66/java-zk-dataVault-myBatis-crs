package ru.masterdm.crs.web.service;

import org.springframework.stereotype.Service;
import org.zkoss.zul.ListModelList;

import ru.masterdm.crs.domain.entity.criteria.Conjunction;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.Where;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.web.domain.entity.EntityFilter;

/**
 * Criteria builder service implementation.
 * @author Igor Matushak
 */
@Service("criteriaBuilderUiService")
public class CriteriaBuilderUiServiceImpl implements CriteriaBuilderUiService {

    @Override
    public Criteria getCriteriaByEntityFilters(ListModelList<EntityFilter> entityFilters, EntityMetaService entityMetaService) {
        Criteria criteria = new Criteria();
        Where where = criteria.getWhere();
        for (EntityFilter entityFilter : entityFilters) {
            Operator operator = entityFilter.getOperator();

            if (entityFilter.getReferencedAttributeMetaPair().getReferencedAttributeMeta() == null) {
                if (entityFilter.getReferencedAttributeMetaPair().getAttributeMeta().getType().equals(AttributeType.REFERENCE)) {
                    where.setReferenceExists(entityFilter.getReferencedAttributeMetaPair().getAttributeMeta(),
                                             operator.equals(Operator.IS_NOT_NULL) ? Conjunction.AND : Conjunction.AND_NOT);
                } else {
                    where.addItem(new WhereItem(entityFilter.getReferencedAttributeMetaPair().getAttributeMeta(), operator, entityFilter.getValue()));
                }
            } else {
                EntityMeta referencedEntityMeta =
                        entityMetaService
                                .getEntityMetaByKeyNoCache(entityFilter.getReferencedAttributeMetaPair().getAttributeMeta().getEntityKey(), null);
                String referencedAttributeMetaKey = entityFilter.getReferencedAttributeMetaPair().getReferencedAttributeMeta().getKey();

                AttributeMeta referencedAttributeMeta = referencedEntityMeta.getAttributeMetadata(referencedAttributeMetaKey);

                criteria.getWhere().addReferenceItem(entityFilter.getReferencedAttributeMetaPair().getAttributeMeta(),
                                                     new WhereItem(Conjunction.AND, referencedAttributeMeta,
                                                                   operator,
                                                                   entityFilter.getValue()));
            }

        }
        return criteria;
    }
}
