package ru.masterdm.crs.service.calc;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.InputFormAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.CalculationProfileAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.service.EntityMetaService;

/**
 * Factory method to create prototype metadata for input form entity.
 * <p>No names and comments members initialised for entity and attribute metadata. Human readable members should be initialised by caller of this
 * method.</p>
 * <p>Entity metadata contains common attributes for classifier (see. {@link InputFormAttributeMeta})</p>
 * @author Pavel Masalov
 */
@Service("inputFormPrototypeFactory")
public class InputFormPrototypeFactory implements EntityMetaPrototypeFactory {

    @Autowired
    private EntityMetaService entityMetaService;

    @Override
    public EntityMeta create(String entityMetaKey) {
        EntityMeta entityMeta = new EntityMeta();
        if (!StringUtils.isEmpty(entityMetaKey))
            entityMeta.setKey(entityMetaKey.trim().toUpperCase());
        entityMeta.setTypes(Arrays.asList(EntityType.INPUT_FORM));

        AttributeMeta calculationAttributeMetaRef = new AttributeMeta();
        calculationAttributeMetaRef.setKey(entityMetaService.getAttributeMetaKey(entityMeta, InputFormAttributeMeta.CALC.name()));
        calculationAttributeMetaRef.setType(AttributeType.REFERENCE);
        calculationAttributeMetaRef.setEntityKey(Calculation.METADATA_KEY);
        calculationAttributeMetaRef.setAttributeKey(Calculation.CalculationAttributeMeta.NAME.getKey());
        entityMeta.getAttributes().add(calculationAttributeMetaRef);

        AttributeMeta profileAttributeMeta = new AttributeMeta();
        profileAttributeMeta.setKey(entityMetaService.getAttributeMetaKey(entityMeta, InputFormAttributeMeta.CALC_PROFILE.name()));
        profileAttributeMeta.setType(AttributeType.REFERENCE);
        profileAttributeMeta.setEntityKey(CalculationProfileAttributeMeta.METADATA_KEY);
        profileAttributeMeta.setAttributeKey(CalculationProfileAttributeMeta.NAME.getKey());
        entityMeta.getAttributes().add(profileAttributeMeta);

        return entityMeta;
    }
}
