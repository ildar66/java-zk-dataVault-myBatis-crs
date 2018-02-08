package ru.masterdm.crs.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.FormulaResult;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.CalculationProfileAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.CommonAttribute;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.domain.form.CreateOption;
import ru.masterdm.crs.domain.form.FileFormat;
import ru.masterdm.crs.domain.form.FormTemplate;
import ru.masterdm.crs.domain.form.RepeatType;
import ru.masterdm.crs.domain.form.TemplateType;
import ru.masterdm.crs.domain.form.mapping.Mapper;
import ru.masterdm.crs.domain.form.mapping.MappingField;
import ru.masterdm.crs.domain.form.mapping.MappingObject;
import ru.masterdm.crs.domain.form.mapping.Range;
import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.util.annotation.CurrentTimeStamp;

/**
 * Form template service implementation.
 * @author Pavel Masalov
 */
@Validated
@Service("formTemplateService")
public class FormTemplateServiceImpl implements FormTemplateService {

    @Autowired
    private EntityService entityService;
    @Autowired
    private EntityMetaService entityMetaService;

    private static ObjectMapper objectMapper;

    @Transactional
    @Override
    public void persistFormTemplate(FormTemplate formTemplate) {
        mapperToJsonConfig(formTemplate);
        entityService.persistEntity(formTemplate);
    }

    @Override
    public List<FormTemplate> getFormTemplates(Criteria criteria, RowRange rowRange, @CurrentTimeStamp LocalDateTime ldts) {
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(FormTemplate.METADATA_KEY, ldts);
        List<FormTemplate> formTemplateList = (List<FormTemplate>) entityService.getEntities(entityMeta, criteria, rowRange, ldts);
        for (FormTemplate ft : formTemplateList) {
            jsonConfigToMapper(ft);
        }
        return formTemplateList;
    }

    @Override
    public void mapperToJsonConfig(@NotNull FormTemplate formTemplate) {
        if (formTemplate.getMapper() == null) {
            formTemplate.setMapperConfig(null);
            return;
        }

        // object -> json
        try {
            String json = getObjectMapper().writeValueAsString(formTemplate.getMapper());
            formTemplate.setMapperConfig(json);
        } catch (JsonProcessingException e) {
            throw new CrsException("FormTemplate key:" + formTemplate.getKey() + " config JSONing error", e);
        }
    }

    @Override
    public void jsonConfigToMapper(@NotNull FormTemplate formTemplate) {
        if (StringUtils.isEmpty(formTemplate.getMapperConfig())) {
            formTemplate.setMapper(null);
            return;
        }
        // json -> object
        Mapper m;
        try {
            m = getObjectMapper().readValue(formTemplate.getMapperConfig(), Mapper.class);
        } catch (Exception e) {
            throw new CrsException("FormTemplate key:" + formTemplate.getKey() + " config deJSONing error", e);
        }

        postDeserialize(m);
        formTemplate.setMapper(m);
    }

    /**
     * Executed after deserialization by enriching data.
     * Add parent relations to mappers and {@link MappingField mapping fields}.
     * Add metadata.
     * @param mapper mapper to process
     */
    private void postDeserialize(Mapper mapper) {
        if (mapper instanceof MappingObject) {
            MappingObject mappingObject = (MappingObject) mapper;
            mappingObject.setEntityMeta(entityMetaService.getEntityMetaByKey(mappingObject.getEntityMeta().getKey(), null));
            for (MappingField mappingField : mappingObject.getFields()) {
                mappingField.setMappingObject(mappingObject);
                if (mappingField.getAttributeMeta().getKey().equals(CommonAttribute.KEY.name()))
                    mappingField.setAttributeMeta(mappingObject.getEntityMeta().getKeyAttribute());
                else if (mappingObject.getEntityMeta().getKey().equals(FormulaResult.METADATA_KEY)
                         && mappingField.getAttributeMeta().getKey().equals(Calculation.CalculationAttributeMeta.CALC_PROFILE.getKey())) {
                    EntityMeta calcEntityMeta = entityMetaService.getEntityMetaByKey(Calculation.METADATA_KEY, null);
                    String attributeKey = entityMetaService.getAttributeMetaKey(calcEntityMeta, CalculationProfileAttributeMeta.METADATA_KEY);
                    mappingField.setAttributeMeta(calcEntityMeta.getAttributeMetadata(attributeKey));
                } else
                    mappingField.setAttributeMeta(mappingObject.getEntityMeta().getAttributeMetadata(mappingField.getAttributeMeta().getKey()));
                if (mappingField.getObject() != null)
                    postDeserialize(mappingField.getObject());
                // TODO now value do not convert mappingField.setValue();
            }
        }

        for (MappingObject ch : mapper.getObjects()) {
            ch.setParent(mapper);
            postDeserialize(ch);
        }
    }

    /**
     * Get JSON object mapper.
     * @return object mapper
     */
    private ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Create and configure object mapper (as singleton).
     * @return object mapper
     */
    private static ObjectMapper newObjectMapper() {
        return new ObjectMapper().configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, false)
                                 .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, true);
    }

    static {
        objectMapper = newObjectMapper();
    }

    @Override
    public List<EntityMeta> getInputForms(List<FormTemplate> formTemplates) {
        List<EntityMeta> inputForms = formTemplates.stream()
                                                   .filter(formTemplate -> formTemplate.getMapper() != null)
                                                   .flatMap(formTemplate -> formTemplate.getMapper().getObjects().stream())
                                                   .filter(mappingObject -> mappingObject.getEntityMeta().getKey().equals(Calculation.METADATA_KEY))
                                                   .flatMap(calculationObject -> calculationObject.getObjects().stream())
                                                   .filter(mappingObject -> mappingObject.getEntityMeta().getType().equals(EntityType.INPUT_FORM))
                                                   .map(formObject -> formObject.getEntityMeta())
                                                   .collect(Collectors.toList());
        return inputForms.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public FormTemplate prepareFormTemplateForEntities(EntityMeta entityMeta, TemplateType type) {
        Mapper mapper = new Mapper();
        MappingObject mappingObject = new MappingObject(entityMeta);
        mappingObject.setRepeat(RepeatType.DOWN);
        mapFields(mappingObject);
        mapper.addObject(mappingObject);
        if (type.equals(TemplateType.IMPORT)) {
            mappingObject.setCreateOption(CreateOption.IF_NOT_EXISTS);
            mappingObject.setUpdateOption(true);
            mappingObject.getFields().get(0).setKey(true);
        } else
            mappingObject.getFields()
                         .stream()
                         .filter(mappingField -> mappingField.getAttributeMeta().getType().equals(AttributeType.REFERENCE))
                         .forEach(mappingField -> {
                             EntityMeta entityMetaRef = entityMetaService.getEntityMetaByKey(mappingField.getAttributeMeta().getEntityKey(), null);
                             MappingObject mappingObjectRef = new MappingObject(entityMetaRef);
                             mappingObjectRef.setRepeat(RepeatType.DOWN);
                             mapFields(mappingObjectRef);
                             mapper.addObject(mappingObjectRef);
                         });
        FormTemplate formTemplate = new FormTemplate();
        formTemplate.setMeta(entityMetaService.getEntityMetaByKey(FormTemplate.METADATA_KEY, null));
        formTemplate.setType(type);
        formTemplate.setFormat(FileFormat.EXCEL);
        formTemplate.setName(entityMeta.getName());
        formTemplate.setMapper(mapper);
        return formTemplate;
    }

    /**
     * Maps entities fields to range.
     * @param mappingObject mapping object
     */
    private void mapFields(MappingObject mappingObject) {
        for (int i = 0; i < mappingObject.getFields().size(); i++) {
            MappingField mappingField = mappingObject.getFields().get(i);
            mappingField.setMapped(true);
            Range range = new Range(mappingObject.getEntityMeta().getKey(), 1, i);
            mappingField.setRange(range);
        }
    }
}
