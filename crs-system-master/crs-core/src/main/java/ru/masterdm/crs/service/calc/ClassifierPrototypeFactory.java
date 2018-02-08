package ru.masterdm.crs.service.calc;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.ClassifierAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.CalculationProfileAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.service.EntityMetaService;

/**
 * Factory o create prototype metadata for classifier entity.
 * <p>No names and comments members initialised for entity and attribute metadata. Human readable members should be initialised by caller of this
 * method.</p>
 * <p>Entity metadata contains common attributes for classifier (see. {@link ClassifierAttributeMeta})</p>
 * <p>Attribute type for {@link ClassifierAttributeMeta#CLASSIFIER_TYPE} should be specified by caller</p>
 * @author Pavel Masalov
 */
@Service("classifierPrototypeFactory")
public class ClassifierPrototypeFactory implements EntityMetaPrototypeFactory {

    @Autowired
    private EntityMetaService entityMetaService;

    @Override
    public EntityMeta create(String entityMetaKey) {
        EntityMeta entityMeta = new EntityMeta();
        if (!StringUtils.isEmpty(entityMetaKey))
            entityMeta.setKey(entityMetaKey.trim().toUpperCase());
        entityMeta.setTypes(Arrays.asList(EntityType.CLASSIFIER));

        AttributeMeta calculationAttributeMetaRef = new AttributeMeta();
        calculationAttributeMetaRef.setKey(entityMetaService.getAttributeMetaKey(entityMeta, ClassifierAttributeMeta.CALC.name()));
        calculationAttributeMetaRef.setType(AttributeType.REFERENCE);
        calculationAttributeMetaRef.setEntityKey(Calculation.METADATA_KEY);
        calculationAttributeMetaRef.setAttributeKey(Calculation.CalculationAttributeMeta.NAME.getKey());
        entityMeta.getAttributes().add(calculationAttributeMetaRef);

        AttributeMeta typeAttr = new AttributeMeta();
        typeAttr.setKey(entityMetaService.getAttributeMetaKey(entityMeta, ClassifierAttributeMeta.CLASSIFIER_TYPE.name()));
        typeAttr.setNullable(true);
        typeAttr.setNativeColumn(ClassifierAttributeMeta.CLASSIFIER_TYPE.name());
        entityMeta.getAttributes().add(typeAttr);

        AttributeMeta commentAttr = new AttributeMeta();
        commentAttr.setKey(entityMetaService.getAttributeMetaKey(entityMeta, ClassifierAttributeMeta.CLASSIFIER_COMMENT.name()));
        commentAttr.setType(AttributeType.TEXT);
        commentAttr.setNativeColumn(ClassifierAttributeMeta.CLASSIFIER_COMMENT.name());
        commentAttr.setNullable(true);
        entityMeta.getAttributes().add(commentAttr);

        AttributeMeta profileAttributeMetaRef = new AttributeMeta();
        profileAttributeMetaRef.setKey(entityMetaService.getAttributeMetaKey(entityMeta, ClassifierAttributeMeta.CALC_PROFILE.name()));
        profileAttributeMetaRef.setType(AttributeType.REFERENCE);
        profileAttributeMetaRef.setEntityKey(CalculationProfileAttributeMeta.METADATA_KEY);
        profileAttributeMetaRef.setAttributeKey(CalculationProfileAttributeMeta.NAME.getKey());
        entityMeta.getAttributes().add(profileAttributeMetaRef);

        return entityMeta;
    }
}
