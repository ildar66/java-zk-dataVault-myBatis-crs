package ru.masterdm.crs.service.calc.formula;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.ClassifierAttributeMeta;
import ru.masterdm.crs.domain.calc.InputFormAttributeMeta;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.Where;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.CommonAttribute;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.util.annotation.CurrentTimeStamp;

/**
 * Functions to execute by javascript runned at nashorn.
 * @author Pavel Masalov
 */
@Service
@Validated
public class SimpleDataAccessServiceImpl implements SimpleDataAccessService {

    @Autowired
    private EntityService entityService;
    @Autowired
    private EntityMetaService entityMetaService;

    @Override
    public List<?> lookupData(@CurrentTimeStamp LocalDateTime modelActuality, @CurrentTimeStamp LocalDateTime dataActuality,
                              @NotNull Calculation calculation, @NotNull Entity profile, @NotNull String entityMetaKey,
                              ScriptObjectMirror... paramPairs) {
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(entityMetaKey, modelActuality);
        if (entityMeta == null)
            throw new CrsException("No entity meta for key " + entityMetaKey);

        Criteria criteria = new Criteria();
        Where where = criteria.getWhere();
        for (ScriptObjectMirror paramPair : paramPairs) {
            if (paramPair == null) {
                continue;
            }
            for (String attributeMetaKey : paramPair.getOwnKeys(true)) {
                Object attributeValue = paramPair.get(attributeMetaKey);
                AttributeMeta attributeMeta = attributeMetaKey.equals(CommonAttribute.KEY.name()) ? entityMeta.getKeyAttribute()
                                                                                                  : entityMeta.getAttributeMetadata(attributeMetaKey);
                if (attributeMeta == null)
                    throw new CrsException("No attribute '" + attributeMetaKey + "' in entity meta " + entityMetaKey);

                if (attributeValue instanceof ScriptObjectMirror) {
                    if (attributeMeta.getType() != AttributeType.REFERENCE)
                        throw new CrsException("Entity meta " + entityMeta.getKey() + " attribute " + attributeMeta.getKey() + " is not of type "
                                               + AttributeType.REFERENCE.name() + ". Reference query not supported for type "
                                               + attributeMeta.getType().name());
                    addReferenceItems(criteria, entityMeta, attributeMeta, (ScriptObjectMirror) attributeValue, modelActuality);
                } else {
                    where.addItem(new WhereItem(attributeMeta, Operator.EQ, attributeValue));
                }
            }
        }

        AttributeMeta calcReferenceAttribute = null;
        AttributeMeta profileReferenceAttribute = null;
        if (entityMeta.getTypes().contains(EntityType.CLASSIFIER)) {
            calcReferenceAttribute = entityMeta.getAttributeMetadata(entityMetaService.getAttributeMetaKey(entityMeta,
                                                                                                           ClassifierAttributeMeta.CALC.name()));
            profileReferenceAttribute = entityMeta.getAttributeMetadata(entityMetaService.getAttributeMetaKey(entityMeta,
                                                                                                              ClassifierAttributeMeta.CALC_PROFILE
                                                                                                                      .name()));

        } else if (entityMeta.getTypes().contains(EntityType.INPUT_FORM)) {
            calcReferenceAttribute = entityMeta.getAttributeMetadata(entityMetaService.getAttributeMetaKey(entityMeta,
                                                                                                           InputFormAttributeMeta.CALC.name()));
            profileReferenceAttribute = entityMeta.getAttributeMetadata(entityMetaService.getAttributeMetaKey(entityMeta,
                                                                                                              InputFormAttributeMeta.CALC_PROFILE
                                                                                                                      .name()));
        }

        if (calcReferenceAttribute != null && calculation != null) {
            criteria.addReferencedEntity(calculation);
        }

        if (profileReferenceAttribute != null && profile != null) {
            criteria.addReferencedEntity(profile);
        }

        return entityService.getEntities(entityMeta, criteria, null, dataActuality, modelActuality);
    }

    /**
     * Compose referenced attributes into query criteria.
     * @param criteria query criteria
     * @param entityMeta entity metadata
     * @param referenceAttribute parent reference attribute
     * @param childAttributeValues scope of child values array
     * @param metadataLdts metadata load datetime
     */
    private void addReferenceItems(Criteria criteria, EntityMeta entityMeta, AttributeMeta referenceAttribute,
                                   ScriptObjectMirror childAttributeValues, LocalDateTime metadataLdts) {
        EntityMeta metaChild = entityMetaService.getEntityMetaByKey(referenceAttribute.getEntityKey(), metadataLdts);
        if (metaChild == null)
            throw new CrsException("No referenced entity meta for key " + referenceAttribute.getEntityKey());

        Where where = criteria.getWhere();

        for (Object childObject : childAttributeValues.values()) {
            ScriptObjectMirror childScriptObject = (ScriptObjectMirror) childObject;
            String attributeMetaKey = childScriptObject.getOwnKeys(true)[0];
            Object attributeValue = childScriptObject.get(attributeMetaKey);
            AttributeMeta attributeMeta = metaChild.getAttributeMetadata(attributeMetaKey);
            if (attributeMeta == null)
                throw new CrsException(
                        "No attribute '" + attributeMetaKey + "' at referenced entity meta " + metaChild.getKey() + " referenced from entity "
                        + entityMeta.getKey() + " attribute " + referenceAttribute.getKey());
            where.addReferenceItem(referenceAttribute, new WhereItem(attributeMeta, Operator.EQ, attributeValue));
        }
    }
}
