package ru.masterdm.crs.service.calc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ru.masterdm.crs.dao.calc.CalcDao;
import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.ClassifierAttributeMeta;
import ru.masterdm.crs.domain.calc.Formula;
import ru.masterdm.crs.domain.calc.FormulaResult;
import ru.masterdm.crs.domain.calc.InputFormAttributeMeta;
import ru.masterdm.crs.domain.calc.Model;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.entity.EntityDbService;

/**
 * Helper service implementation.
 * @author Pavel Masalov
 */
@Service
public class CalculationServiceImpl implements CalculationService {

    @Autowired
    private CalcDao calcDao;
    @Autowired
    private EntityDbService entityDbService;
    @Autowired
    private EntityService entityService;
    @Autowired
    private EntityMetaService entityMetaService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistsFormulaResultsAndTrinityLinks(Calculation calculation, Formula formulaAndResult, Entity profile, LocalDateTime ldts) {
        persistsFormulaResult(formulaAndResult.getFormulaResult(calculation, profile), ldts);
        calcDao.writeLinkCalcFormulaResult(calculation, formulaAndResult, formulaAndResult.getFormulaResult(calculation, profile), profile, false,
                                           ldts);
    }

    @Override
    @Transactional
    public void persistsFormulaResult(FormulaResult formulaResult, LocalDateTime ldts) {
        entityService.persistEntity(formulaResult);
    }

    @Override
    public List<Entity> getClassifierValues(Calculation calculation, LocalDateTime ldts) {
        Model model = calculation.getModel();
        if (model == null)
            return Collections.emptyList();

        List<Entity> classifierValues = Collections.synchronizedList(new ArrayList());
        model.getClassifiers().parallelStream().forEach(classifier -> {
            AttributeMeta calcReferenceAttributeMeta =
                    classifier.getAttributeMetadata(entityMetaService.getAttributeMetaKey(classifier, ClassifierAttributeMeta.CALC.name()));
            if (calcReferenceAttributeMeta != null) {
                List<Long> classifierValueHubIds = entityDbService.readEntityAttributeBackLink(calculation, calcReferenceAttributeMeta,
                                                                                               classifier, ldts);
                if (!classifierValueHubIds.isEmpty()) {
                    Criteria criteria = new Criteria();
                    criteria.setHubIds(classifierValueHubIds);
                    List<Entity> classifierValuesL = (List<Entity>) entityService.getEntities(classifier, criteria, null, ldts);
                    classifierValues.addAll(classifierValuesL);
                }
            }
        });
        return classifierValues;
    }

    @Override
    public List<Entity> getInputFormValues(Calculation calculation, EntityMeta inputForm, Criteria criteria, RowRange rowRange, LocalDateTime ldts) {
        AttributeMeta calcReferenceAttributeMeta =
                inputForm.getAttributeMetadata(entityMetaService.getAttributeMetaKey(inputForm, InputFormAttributeMeta.CALC.name()));
        if (calcReferenceAttributeMeta != null) {
            List<Long> inputFormValueHubIds = entityDbService.readEntityAttributeBackLink(calculation, calcReferenceAttributeMeta, inputForm, ldts);
            if (!inputFormValueHubIds.isEmpty()) {
                if (criteria == null) {
                    criteria = new Criteria();
                }
                criteria.setHubIds(inputFormValueHubIds);
                return (List<Entity>) entityService.getEntities(inputForm, criteria, rowRange, ldts);
            }
        }
        if (rowRange != null) {
            rowRange.setTotalCount(0L);
        }
        return Collections.emptyList();
    }

    @Transactional
    @Override
    public void persistsClassifierValues(Calculation calculation, List<Entity> classifierValues) {
        Map<EntityMeta, List<Entity>> entityByMeta = classifierValues.stream().collect(Collectors.groupingBy(Entity::getMeta, Collectors.toList()));

        for (Map.Entry<EntityMeta, List<Entity>> entry : entityByMeta.entrySet()) {
            EntityMeta entityMeta = entry.getKey();
            AttributeMeta calcReferenceAttribute =
                    entityMeta.getAttributeMetadata(entityMetaService.getAttributeMetaKey(entityMeta, ClassifierAttributeMeta.CALC.name()));
            for (Entity entity : entry.getValue()) {
                LinkedEntityAttribute calcRef = (LinkedEntityAttribute) entity.getAttribute(calcReferenceAttribute.getKey());
                if (!calcRef.isExists(calculation)) {
                    calcRef.add(calculation);
                }
                entityService.persistEntity(entity);
            }
        }
    }

    @Transactional
    @Override
    public void persistsInputFormValues(Calculation calculation, EntityMeta inputForm, List<Entity> inputFormValues) {
        AttributeMeta calcReferenceAttribute =
                inputForm.getAttributeMetadata(entityMetaService.getAttributeMetaKey(inputForm, InputFormAttributeMeta.CALC.name()));

        for (Entity entity : inputFormValues) {
            LinkedEntityAttribute calcRef = (LinkedEntityAttribute) entity.getAttribute(calcReferenceAttribute.getKey());
            if (!calcRef.isExists(calculation)) {
                calcRef.add(calculation);
            }
            entityService.persistEntity(entity);
        }
    }
}
