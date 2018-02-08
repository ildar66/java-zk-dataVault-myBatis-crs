package ru.masterdm.crs.web.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zkoss.util.Locales;
import org.zkoss.util.media.AMedia;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zss.api.Exporter;
import org.zkoss.zss.api.Exporters;
import org.zkoss.zss.api.Importers;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.CellData;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Window;

import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.Formula;
import ru.masterdm.crs.domain.calc.FormulaResult;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.attribute.AbstractAttribute;
import ru.masterdm.crs.domain.entity.attribute.EntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.MultilangAttribute;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.Where;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.AttributeLocale;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.CalculationProfileAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.CommonAttribute;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.domain.form.CreateOption;
import ru.masterdm.crs.domain.form.ExcelFormat;
import ru.masterdm.crs.domain.form.FormDateType;
import ru.masterdm.crs.domain.form.FormTemplate;
import ru.masterdm.crs.domain.form.RepeatType;
import ru.masterdm.crs.domain.form.TemplateType;
import ru.masterdm.crs.domain.form.mapping.ImportObject;
import ru.masterdm.crs.domain.form.mapping.Mapper;
import ru.masterdm.crs.domain.form.mapping.MappingField;
import ru.masterdm.crs.domain.form.mapping.MappingObject;
import ru.masterdm.crs.domain.form.mapping.Range;
import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.service.CalcService;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.FormTemplateService;
import ru.masterdm.crs.service.SecurityService;
import ru.masterdm.crs.service.ValueConvertService;
import ru.masterdm.crs.util.converter.StringToLocalDateConverter;

/**
 * Form templates service implementation.
 * @author Vladimir Shvets
 */
@Service("formTemplateUiService")
public class FormTemplateUiServiceImpl implements FormTemplateUiService {

    @Autowired
    private EntityService entityService;
    @Autowired
    private EntityMetaService entityMetaService;
    @Autowired
    private ValueConvertService valueConvertService;
    @Autowired
    private CalcService calcService;
    @Autowired
    private Properties pages;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private FormTemplateService formTemplateService;

    private static final int MONTHS_IN_QUARTER = 3;
    private static final String ATTRIBUTES_SEPARATOR = "&";

    private Map<String, Integer> rowsMap = new HashMap<>();
    private Map<String, Integer> columnsMap = new HashMap<>();
    private Map<String, Entity> calculationProfilesMap;

    /**
     * Returns calculation profiles map.
     * @return calculation profiles map
     */
    public Map<String, Entity> getCalculationProfilesMap() {
        if (calculationProfilesMap == null) {
            EntityMeta profileMeta = entityMetaService.getEntityMetaByKey(CalculationProfileAttributeMeta.METADATA_KEY, null);
            calculationProfilesMap = entityService.getEntitiesBase(profileMeta, null, null, null)
                                                  .stream().collect(Collectors.toMap(v -> v.getKey(), v -> v));
        }
        return calculationProfilesMap;
    }

    @Override
    public List<ImportObject> prepareFormMap(Book book, Mapper mapper, TemplateType type) {
        prepareRowsColumnsMap(book);
        return prepareFormMap(book, mapper, null, null, type);
    }

    @Override
    public List<ImportObject> prepareFormMap(Book book, Mapper mapper, Calculation context, TemplateType type) {
        prepareRowsColumnsMap(book);
        return prepareFormMap(book, mapper, null, context, type);
    }

    @Override
    public void exportForm(Book book, List<ImportObject> importObjects) {
        importObjects.stream()
                     .filter(importObject -> importObject.getEntity() != null)
                     .forEach(importObject -> importObject.getValuesMap().forEach((key, value) -> {
                                  Sheet sheet = book.getSheet(value.getSheet());
                                  org.zkoss.zss.api.Range range = Ranges.range(sheet, value.getRow(), value.getColumn());
                                  if (!range.getCellData().getType().equals(CellData.CellType.FORMULA)) {
                                      String[] attributeKeys = key.split(ATTRIBUTES_SEPARATOR);
                                      String attributeKey = attributeKeys[attributeKeys.length - 1];
                                      Entity entity = importObject.getEntity();
                                      if (attributeKeys.length > 1) {
                                          int i = 0;
                                          while ((i < attributeKeys.length - 1) && entity != null) {
                                              LinkedEntityAttribute attr = (LinkedEntityAttribute) entity.getAttribute(attributeKeys[i]);
                                              if (attr.getValue().size() > 0)
                                                  entity = (Entity) attr.getEntityList().get(0);
                                              else
                                                  entity = null;
                                              i++;
                                          }
                                      }
                                      if (entity != null) {
                                          if (entity.getMeta().getKeyAttribute().getKey().equals(attributeKey))
                                              range.setCellValue(entity.getKey());
                                          else if (entity.getAttributeMetadata(attributeKey).getType().equals(AttributeType.REFERENCE)) {
                                              if (entity.getMeta().getKey().equals(Calculation.METADATA_KEY)
                                                  && (attributeKey.equals(Calculation.CalculationAttributeMeta.AUTHOR.getKey())
                                                      || attributeKey.equals(Calculation.CalculationAttributeMeta.MODEL.getKey()))) {
                                                  if (attributeKey.equals(Calculation.CalculationAttributeMeta.AUTHOR.getKey()))
                                                      range.setCellValue(((Calculation) entity).getAuthor().getFullName());
                                                  else if (attributeKey.equals(Calculation.CalculationAttributeMeta.MODEL.getKey()))
                                                      range.setCellValue(((Calculation) entity)
                                                                                 .getModel().getName()
                                                                                 .getDescription(AttributeLocale.getLocale(Locales.getCurrent())));
                                              } else {
                                                  LinkedEntityAttribute attr = (LinkedEntityAttribute) entity.getAttribute(attributeKey);
                                                  if (attr.getValue().size() > 0)
                                                      if (attr.getMeta().isMultilang())
                                                          range.setCellValue(convert(
                                                                  ((MultilangAttribute) attr.getValue().get(0))
                                                                          .getValue(AttributeLocale.getLocale(Locales.getCurrent()))));
                                                      else {
                                                          AbstractAttribute attrRef = ((Entity) ((EntityAttribute) attr.getValue().get(0))
                                                                  .getEntity()).getAttribute(attr.getMeta().getAttributeKey());
                                                          if (attrRef.getMeta().isMultilang())
                                                              range.setCellValue(
                                                                      convert(((MultilangAttribute) attrRef).getValue(AttributeLocale.getLocale(
                                                                              Locales.getCurrent()))));
                                                          else
                                                              range.setCellValue(convert(((EntityAttribute) attr.getValue().get(0)).getValue()));
                                                      }
                                              }
                                          } else if (entity.getAttributeMetadata(attributeKey).isMultilang())
                                              range.setCellValue(convert(((MultilangAttribute) entity.getAttribute(attributeKey)).getValue(
                                                      AttributeLocale.getLocale(Locales.getCurrent()))));
                                          else
                                              range.setCellValue(convert(entity.getAttributeValue(attributeKey)));
                                      }
                                  }
                              })
                     );
    }

    /**
     * Clears rows and columns maps.
     * @param book book
     */
    private void prepareRowsColumnsMap(Book book) {
        for (int i = 0; i < book.getNumberOfSheets(); i++) {
            rowsMap.put(book.getSheetAt(i).getSheetName(), 0);
            columnsMap.put(book.getSheetAt(i).getSheetName(), 0);
        }
    }

    /**
     * Converts attribute's value (LocalDate) to Date for writing to Excel file.
     * @param value value
     * @return converted value
     */
    private Object convert(Object value) {
        if (value instanceof LocalDate) {
            return Date.from(((LocalDate) value).atStartOfDay(ZoneId.systemDefault()).toInstant());
        } else if (value instanceof LocalDateTime)
            return Date.from(((LocalDateTime) value).atZone(ZoneId.systemDefault()).toInstant());
        return value;
    }

    @Override
    public void importForm(Book book, List<ImportObject> importObjects) {
        importObjects.forEach(importObject -> {
            if (importObject.getMappingObject().getCreateOption().equals(CreateOption.ALWAYS))
                importObject.setEntity(null);
            if (importObject.getEntity() == null && !importObject.getMappingObject().getCreateOption().equals(CreateOption.NEVER)) {
                importObject.setEntity(entityService.newEmptyEntity(importObject.getMappingObject().getEntityMeta()));
                Criteria criteria = new Criteria();
                prepareWhere(criteria, importObject.getMappingObject(), Collections.singletonList(importObject), importObject.getParent());
                criteria.getWhere().getReferenceWheres().forEach((key, value) -> {
                    LinkedEntityAttribute linkedAttribute = (LinkedEntityAttribute) importObject.getEntity().getAttribute(key.getKey());
                    if (importObject.getParent() != null
                        && key.getEntityKey().equals(importObject.getParent().getEntity().getMeta().getKey())) {
                        linkedAttribute.add(importObject.getParent().getEntity());
                    } else {
                        Criteria criteriaRef = new Criteria();
                        criteriaRef.getWhere().getContent().addAll(value.getContent());
                        List<? extends Entity> entitiesRef = entityService
                                .getEntitiesBase(entityMetaService.getEntityMetaByKey(key.getEntityKey(), null),
                                                 criteriaRef, null, null);
                        linkedAttribute.getEntityAttributeList().clear();
                        entitiesRef.forEach(entityRef -> linkedAttribute.add(entityRef));
                    }
                });
                criteria.getReferencedEntities().forEach(refEntity -> {
                    String childEntityKey = refEntity.getMeta().getKey();
                    AttributeMeta key = importObject.getMappingObject().getEntityMeta().getAttributes().stream()
                                                    .filter(a -> a.getType() == AttributeType.REFERENCE
                                                                 && childEntityKey.equals(a.getEntityKey()))
                                                    .findFirst().orElse(null);
                    LinkedEntityAttribute linkedAttribute = (LinkedEntityAttribute) importObject.getEntity().getAttribute(key.getKey());
                    if (importObject.getParent() != null
                        && key.getEntityKey().equals(importObject.getParent().getEntity().getMeta().getKey())) {
                        linkedAttribute.add(importObject.getParent().getEntity());
                    } else {
                        Criteria criteriaRef = new Criteria();
                        EntityMeta entityMetaByKey = entityMetaService.getEntityMetaByKey(key.getEntityKey(), null);
                        criteriaRef.getWhere().addItem(new WhereItem(entityMetaByKey.getHubIdAttribute(), Operator.EQ, refEntity.getHubId()));
                        List<? extends Entity> entitiesRef = entityService.getEntitiesBase(entityMetaByKey, criteriaRef, null, null);
                        linkedAttribute.getEntityAttributeList().clear();
                        entitiesRef.forEach(entityRef -> linkedAttribute.add(entityRef));
                    }
                });
                criteria.getWhere().getContent().forEach(connectable -> {
                    WhereItem whereItem = (WhereItem) connectable;
                    setCommonAttributeValue(importObject.getEntity(), whereItem.getSearchAttribute().getKey(),
                                            valueConvertService.convert(whereItem.getSearchAttribute(), whereItem.getValue()));
                });
            }
            importObject.setModified(false);
            if (importObject.getEntity() != null && (importObject.getMappingObject().getUpdateOption()
                                                     || importObject.getEntity().getKey() == null)) {
                importObject.getValuesMap().forEach((key, value) -> {
                    Sheet sheet = book.getSheet(value.getSheet());
                    org.zkoss.zss.api.Range range = Ranges.range(sheet, value.getRow(), value.getColumn());
                    AttributeMeta attributeMeta = importObject.getEntity().getAttributeMetadata(key);
                    if (attributeMeta.getType().equals(AttributeType.REFERENCE)) {
                        LinkedEntityAttribute linkedAttribute = (LinkedEntityAttribute) importObject.getEntity().getAttribute(key);
                        AttributeMeta linkedAttributeMeta = entityMetaService.getEntityMetaByKey(attributeMeta.getEntityKey(), null)
                                                                             .getAttributeMetadata(attributeMeta.getAttributeKey());
                        Criteria criteriaRef = new Criteria();
                        criteriaRef.getWhere().addItem(new WhereItem(linkedAttributeMeta, Operator.EQ,
                                                                     valueConvertService.convert(linkedAttributeMeta,
                                                                                                 getCellValue(range,
                                                                                                              linkedAttributeMeta.getType()))));
                        List<? extends Entity> entitiesRef =
                                entityService.getEntitiesBase(entityMetaService.getEntityMetaByKey(attributeMeta.getEntityKey(), null),
                                                              criteriaRef, null, null);
                        linkedAttribute.getEntityAttributeList().clear();
                        entitiesRef.forEach(entityRef -> linkedAttribute.add(entityRef));
                        importObject.setModified(true);
                    } else {
                        Object newValue = valueConvertService.convert(attributeMeta, getCellValue(range, attributeMeta.getType()));
                        if (importObject.getEntity().getAttributeMetadata(key).isMultilang()) {
                            if (ObjectUtils.compare(
                                    (Comparable) ((MultilangAttribute) importObject.getEntity().getAttribute(key)).getValue(
                                            AttributeLocale.getLocale(Locales.getCurrent())),
                                    (Comparable) newValue) != 0) {
                                if (newValue != null) {
                                    setCommonAttributeValue(importObject.getEntity(), key, newValue);
                                    importObject.setModified(true);
                                }
                            }
                        } else if (ObjectUtils.compare((Comparable) importObject.getEntity().getAttributeValue(key), (Comparable) newValue)
                                   != 0) {
                            if (newValue != null) {
                                setCommonAttributeValue(importObject.getEntity(), key, newValue);
                                importObject.setModified(true);
                            }
                        }
                    }
                });
                if (importObject.getEntity().getMeta().getType().equals(EntityType.INPUT_FORM)) {
                    if (importObject.isModified())
                        entityService.persistEntity(importObject.getEntity());
                } else {
                    setDefaultValue(importObject.getEntity());
                    entityService.persistEntity(importObject.getEntity());
                }
            }
        });
    }

    /**
     * Get value from cell.
     * @param range range
     * @param type type
     * @return cell value
     */
    private Object getCellValue(org.zkoss.zss.api.Range range, AttributeType type) {
        if (type.equals(AttributeType.DATE) || type.equals(AttributeType.DATETIME)) {
            return range.getCellData().getDateValue();
        } else if (type.equals(AttributeType.STRING) || type.equals(AttributeType.TEXT))
            return range.getCellData().getEditText();
        else if (type.equals(AttributeType.FILE))
            return null;
        return range.getCellData().getValue();
    }

    /**
     * Set default entity values.
     * @param entity entity
     */
    private void setDefaultValue(Entity entity) {
        if (entity.getMeta().getKey().equals(Calculation.METADATA_KEY)) {
            if (entity.getAttributeValue(Calculation.CalculationAttributeMeta.DATA_ACTUALITY.getKey()) == null) {
                entity.setAttributeValue(Calculation.CalculationAttributeMeta.DATA_ACTUALITY.getKey(), entityMetaService.getSysTimestamp());
            }
            ((Calculation) entity).setAuthor(securityService.getCurrentUser());
        }
    }

    /**
     * Prepares where.
     * @param criteria criteria
     * @param mappingObject mapping object
     * @param importObjects list of import objects
     * @param parent parent import object
     */
    private void prepareWhere(Criteria criteria, MappingObject mappingObject, List<ImportObject> importObjects, ImportObject parent) {
        Where where = criteria.getWhere();
        if (mappingObject.getParent() instanceof MappingObject) {
            MappingField parentLink = mappingObject.getFields()
                                                   .stream()
                                                   .filter(mappingField -> mappingField.getAttributeMeta().getType().equals(AttributeType.REFERENCE)
                                                                           && mappingField.getAttributeMeta().getEntityKey() != null
                                                                           && mappingField.getAttributeMeta().getEntityKey()
                                                                                          .equals(((MappingObject) mappingObject.getParent())
                                                                                                          .getEntityMeta().getKey()))
                                                   .findFirst().orElse(null);
            if (parentLink != null)
                criteria.addReferencedEntity(parent.getEntity());
        }
        mappingObject.getFields().stream()
                     .filter(MappingField::isKey)
                     .forEach(mappingField -> {
                         if (mappingField.getAttributeMeta().getType().equals(AttributeType.DATE)
                             && mappingObject.getEntityMeta().getType().equals(EntityType.INPUT_FORM)
                             && !mappingField.getFormDateType().equals(FormDateType.CUSTOM)
                             && parent != null
                             && !(mappingField.isMapped() && mappingField.getRange() != null)) {
                             LocalDate date = (LocalDate) ((Calculation) parent.getEntity())
                                     .getAttribute(Calculation.CalculationAttributeMeta.ACTUALITY.getKey())
                                     .getValue();
                             switch (mappingField.getFormDateType()) {
                                 case CURRENT:
                                     mappingField.setValue(date);
                                     break;
                                 case THIS_YEAR_BEGINNING:
                                     mappingField.setValue(LocalDate.of(date.getYear(), 1, 1));
                                     break;
                                 case LAST_YEAR_BEGINNING:
                                     mappingField.setValue(LocalDate.of(date.getYear() - 1, 1, 1));
                                     break;
                                 case OFFSET:
                                     switch (mappingField.getDateOffsetType()) {
                                         case DAYS:
                                             mappingField.setValue(date.minusDays(mappingField.getDateOffset()));
                                             break;
                                         case MONTHS:
                                             mappingField.setValue(date.minusMonths(mappingField.getDateOffset()));
                                             break;
                                         case QUARTERS:
                                             mappingField.setValue(date.minusMonths(mappingField.getDateOffset() * MONTHS_IN_QUARTER));
                                             break;
                                         case YEARS:
                                             mappingField.setValue(date.minusYears(mappingField.getDateOffset()));
                                             break;
                                         default:
                                             mappingField.setValue(date);
                                     }
                                     break;
                                 default:
                                     break;
                             }
                         }
                         if (mappingField.getValue() != null && !mappingField.getValue().toString().equals(""))
                             setCondition(criteria, where, false, mappingField.getAttributeMeta(), Operator.EQ, mappingField.getValue());
                         else {
                             List<String> keys = new ArrayList<>();
                             importObjects.stream()
                                          .filter(importObject -> importObject.getKeysMap().containsKey(mappingField.getAttributeMeta().getKey()))
                                          .forEach(importObject -> keys.add(importObject.getKeysMap().get(mappingField.getAttributeMeta().getKey())));
                             if (keys.size() > 0)
                                 setCondition(criteria, where, true, mappingField.getAttributeMeta(), Operator.IN, keys.toArray());
                         }
                     });
    }

    /**
     * Set where for usual or link attribute.
     * @param criteria criteria
     * @param where where
     * @param mapped condition from Excel
     * @param attributeMeta attribute meta
     * @param operator operator
     * @param values values
     */
    private void setCondition(Criteria criteria, Where where, boolean mapped, AttributeMeta attributeMeta, Operator operator, Object... values) {
        if (attributeMeta.getType().equals(AttributeType.REFERENCE)) {
            EntityMeta linkedEntityMeta = entityMetaService.getEntityMetaByKey(attributeMeta.getEntityKey(), null);
            AttributeMeta linkedAttributeMeta = (attributeMeta.getAttributeKey() != null && mapped)
                                                ? linkedEntityMeta.getAttributeMetadata(attributeMeta.getAttributeKey())
                                                : linkedEntityMeta.getKeyAttribute();
            if (linkedAttributeMeta != null) {
                if (CalculationProfileAttributeMeta.METADATA_KEY.equals(attributeMeta.getEntityKey())
                    && operator == Operator.EQ && values != null && values.length == 1 && values[0] instanceof String) {
                    Entity profile = getCalculationProfilesMap().get((String) values[0]);
                    criteria.getReferencedEntities().add(profile);
                } else
                    where.addReferenceItem(attributeMeta, new WhereItem(linkedAttributeMeta, operator, values));
            }
        } else
            where.addItem(new WhereItem(attributeMeta, operator, values));
    }

    /**
     * Sets entity's attribute or key value.
     * @param entity entity
     * @param key key
     * @param value value
     */
    private void setCommonAttributeValue(Entity entity, String key, Object value) {
        if (key.equals(CommonAttribute.KEY.name()))
            entity.setKey(value.toString());
        else if (entity.getAttributeMetadata(key).isMultilang())
            ((MultilangAttribute) entity.getAttribute(key)).setValue(value.toString(), AttributeLocale.getLocale(Locales.getCurrent()));
        else
            entity.setAttributeValue(key, value);
    }

    /**
     * Builds tree of import objects.
     * @param book book
     * @param mapper mapper
     * @param parent parent import object
     * @param context calculation context
     * @param type template type
     * @return list of import object
     */
    private List<ImportObject> prepareFormMap(Book book, Mapper mapper, ImportObject parent, Calculation context, TemplateType type) {
        List<ImportObject> totalObjects = new ArrayList<>();
        mapper.getObjects().forEach(mappingObject -> {
            List<ImportObject> importObjects = new ArrayList<>();
            boolean dataExist;
            int rOffset = 0, cOffset = 0;
            do {
                dataExist = false;
                for (int i = 0; i < mappingObject.getRangeSize(); i++) {
                    ImportObject importObject = new ImportObject(mappingObject, parent);
                    boolean isCreated;
                    if (parent == null || !type.equals(TemplateType.IMPORT))
                        isCreated = prepareImportObject(book, importObject, i, rOffset, cOffset);
                    else
                        isCreated = prepareImportObject(book, importObject, i, parent.getRows().get(rOffset), parent.getColumns().get(cOffset));
                    if (isCreated) {
                        ImportObject importObjectCreated = importObjects.stream().filter(io -> io.getKeysMap().equals(importObject.getKeysMap()))
                                                                        .findFirst()
                                                                        .orElse(null);
                        if (importObjectCreated == null) {
                            if (type.equals(TemplateType.IMPORT)) {
                                importObject.getRows().add(rOffset);
                                importObject.getColumns().add(cOffset);
                            }
                            importObjects.add(importObject);
                        } else {
                            if (mappingObject.getRepeat().equals(RepeatType.DOWN))
                                importObjectCreated.getRows().add(rOffset);
                            else if (mappingObject.getRepeat().equals(RepeatType.RIGHT))
                                importObjectCreated.getColumns().add(cOffset);
                        }
                        dataExist = importObject.getKeysMap().size() > 0;
                    }
                }
                if (mappingObject.getRepeat().equals(RepeatType.DOWN))
                    rOffset++;
                else if (mappingObject.getRepeat().equals(RepeatType.RIGHT))
                    cOffset++;

            } while (!mappingObject.getRepeat().equals(RepeatType.ONE)
                     && type.equals(TemplateType.IMPORT)
                     && ((parent == null && dataExist)
                         || (parent != null && rOffset < parent.getRows().size() && cOffset < parent.getColumns().size())));

            List<? extends Entity> entities;
            if (mappingObject.getEntityMeta().getKey().equals(FormulaResult.METADATA_KEY)
                && parent != null && parent.getEntity() != null
                && parent.getEntity().getMeta().getKey().equals(Calculation.METADATA_KEY)) {

                List<Formula> formulas = ((Calculation) parent.getEntity()).getModel().getFormulas();
                List<FormulaResult> formulaResults = new ArrayList<>();
                MappingField keyField = mappingObject.getFields().stream()
                                                     .filter(MappingField::isKey)
                                                     .filter(field -> field.getAttributeMeta().getKey().equals(CommonAttribute.KEY.name()))
                                                     .filter(field -> !"".equals(field.getValue()))
                                                     .findFirst().orElse(null);
                formulas.stream()
                        .filter(formula -> (keyField == null) || keyField.getValue().toString().toUpperCase().equals(formula.getKey()))
                        .forEach(formula -> {
                            MappingField profileField = mappingObject.getFields()
                                                                     .stream()
                                                                     .filter(mappingField -> mappingField.getAttributeMeta().getKey().equals(
                                                                             Calculation.CalculationAttributeMeta.CALC_PROFILE.getKey()))
                                                                     .findFirst().orElse(null);
                            if (profileField != null) {
                                Entity profile = getCalculationProfilesMap().get(profileField.getValue());
                                if (profile != null) {
                                    FormulaResult formulaResult = formula.getFormulaResult(context, profile);
                                    if (formulaResult != null) {
                                        formulaResult.setKey(formula.getKey());
                                        formulaResults.add(formulaResult);
                                    }
                                }
                            }
                        });
                entities = formulaResults;
            } else if (mappingObject.isContext() && context != null)
                entities = Collections.singletonList(context);
            else {
                Criteria criteria = new Criteria();
                prepareWhere(criteria, mappingObject, importObjects, parent);
                entities = entityService.getEntities(mappingObject.getEntityMeta(), criteria, null, null);
            }
            mapEntities(importObjects, entities);
            importObjects.forEach(importObject -> {
                totalObjects.add(importObject);
                if (mappingObject.getObjects().size() > 0) {
                    importObject.getValuesMap().forEach((key, range) -> {
                        MappingField mappingField = mappingObject.getFields()
                                                                 .stream()
                                                                 .filter(f -> f.getAttributeMeta().getKey().equals(key))
                                                                 .findFirst().orElse(null);
                        if (mappingField != null) {
                            range.setRow(mappingField.getRange().getRow() + rowsMap.get(range.getSheet()));
                            range.setColumn(mappingField.getRange().getColumn() + columnsMap.get(range.getSheet()));
                        }
                    });
                    if (importObject.getEntity() == null && type.equals(TemplateType.IMPORT))
                        importForm(book, Collections.singletonList(importObject));
                    if (importObject.getEntity() != null) {
                        totalObjects.addAll(prepareFormMap(book, mappingObject, importObject, context, type));
                    }
                } else if (type.equals(TemplateType.EXPORT)) {
                    Range range = importObject.getValuesMap().values().stream().findFirst().orElse(null);
                    if (range != null)
                        if (mappingObject.getRepeat().equals(RepeatType.DOWN))
                            rowsMap.put(range.getSheet(), rowsMap.get(range.getSheet()) + 1);
                        else if (mappingObject.getRepeat().equals(RepeatType.RIGHT))
                            columnsMap.put(range.getSheet(), columnsMap.get(range.getSheet()) + 1);
                }
            });
        });
        return totalObjects;
    }

    /**
     * Prepares import object.
     * @param book book
     * @param importObject import object
     * @param n number of cell in range
     * @param rOffset vertical offset for DOWN repeat type
     * @param cOffset horizontal offset for RIGHT repeat type
     * @return true if preparing was successful
     */
    private boolean prepareImportObject(Book book, ImportObject importObject, int n, int rOffset, int cOffset) {
        prepareImportObject(book, importObject, importObject.getMappingObject(), n, rOffset, cOffset, "");
        return importObject.getKeysMap().values().stream().noneMatch(key -> key.equals(""));
    }

    /**
     * Prepares import object.
     * @param book book
     * @param importObject import object
     * @param mappingObject mapping object
     * @param n number of cell in range
     * @param rOffset vertical offset for DOWN repeat type
     * @param cOffset horizontal offset for RIGHT repeat type
     * @param key key
     */
    private void prepareImportObject(Book book, ImportObject importObject, MappingObject mappingObject, int n, int rOffset, int cOffset, String key) {
        mappingObject.getFields().forEach(mappingField -> {
            if ((mappingField.isMapped() || mappingField.isWrite()) && mappingField.getRange() != null) {
                Range fieldRange = mappingField.getRange();
                Sheet sheet = book.getSheet(fieldRange.getSheet());
                if (sheet == null)
                    sheet = book.getSheetAt(0);
                int row = fieldRange.getRow() + rOffset * (fieldRange.getLastRow() - fieldRange.getRow() + 1)
                          + rowsMap.get(sheet.getSheetName());
                int col = fieldRange.getColumn() + cOffset * (fieldRange.getLastColumn() - fieldRange.getColumn() + 1)
                          + columnsMap.get(sheet.getSheetName());
                if (fieldRange.getSize() > 1) {
                    row += (int) (n / (fieldRange.getLastColumn() - fieldRange.getColumn() + 1));
                    col += (n % (fieldRange.getLastColumn() - fieldRange.getColumn() + 1));
                }
                org.zkoss.zss.api.Range bookRange = Ranges.range(sheet, row, col);
                String attributeKey = mappingField.getAttributeMeta().getKey();
                if (!key.isEmpty())
                    attributeKey = key.concat(ATTRIBUTES_SEPARATOR).concat(attributeKey);
                if (mappingField.isKey() && !mappingField.isWrite())
                    importObject.getKeysMap()
                                .put(attributeKey, getCellValue(bookRange, AttributeType.STRING).toString().toUpperCase());
                else
                    importObject.getValuesMap().put(attributeKey, new Range(sheet.getSheetName(), row, col));
            }
            if (mappingField.getObject() != null)
                prepareImportObject(book, importObject, mappingField.getObject(), n, rOffset, cOffset,
                                    key.isEmpty() ? mappingField.getAttributeMeta().getKey()
                                                  : key.concat(ATTRIBUTES_SEPARATOR).concat(mappingField.getAttributeMeta().getKey()));
        });
    }

    /**
     * Matches import objects and returned entities.
     * @param importObjects list of import objects
     * @param entities list of entities
     */
    private void mapEntities(List<ImportObject> importObjects, List<? extends Entity> entities) {
        if (importObjects.size() == 1)
            if (importObjects.get(0).getMappingObject().getRepeat().equals(RepeatType.DOWN)) {
                int row = 0;
                for (Entity entity : entities) {
                    ImportObject importObject;
                    if (row > 0) {
                        importObject = new ImportObject(importObjects.get(0).getMappingObject());
                        importObject.setParent(importObjects.get(0).getParent());
                        importObject.getKeysMap().putAll(importObjects.get(0).getKeysMap());
                        for (Map.Entry<String, Range> entry : importObjects.get(0).getValuesMap().entrySet()) {
                            int newRow = entry.getValue().getRow() + row;
                            Range range = new Range(entry.getValue().getSheet(), newRow, entry.getValue().getColumn());
                            importObject.getValuesMap().put(entry.getKey(), range);
                        }
                        importObjects.add(importObject);
                    } else
                        importObject = importObjects.get(0);
                    importObject.setEntity(entity);
                    if (importObject.getParent() != null)
                        importObject.getParent().getRows().add(row);
                    row += importObjects.get(0).getMappingObject().getRangeSize();
                }
            } else {
                if (entities.size() > 0)
                    importObjects.get(0).setEntity(entities.get(0));
            }
        else
            importObjects.forEach(importObject -> importObject.setEntity(
                    entities.stream()
                            .filter(entity ->
                                            importObject.getKeysMap().entrySet()
                                                        .stream()
                                                        .allMatch(entry -> entry.getValue().equals(getCommonAttributeValue(entity, entry.getKey()))))
                            .findFirst().orElse(null))
            );
    }

    /**
     * Gets entity's attribute or key value.
     * @param entity entity
     * @param key key
     * @return attribute or key value
     */
    private String getCommonAttributeValue(Entity entity, String key) {
        if (key.equals(CommonAttribute.KEY.name()))
            return entity.getKey();
        else if (entity.getAttributeMetadata(key).getType().equals(AttributeType.REFERENCE)) {
            LinkedEntityAttribute attr = (LinkedEntityAttribute) entity.getAttribute(key);
            if (attr.getValue().size() > 0)
                return ((EntityAttribute) attr.getValue().get(0)).getValue().toString();
            else
                return "";
        } else if (entity.getAttributeMetadata(key).getType().equals(AttributeType.DATE)) {
            LocalDate dateValue = (LocalDate) entity.getAttributeValue(key);
            return dateValue.format(DateTimeFormatter.ofPattern(StringToLocalDateConverter.FORMAT));
        } else
            return entity.getAttributeValue(key).toString();
    }

    @Override
    public void process(FormTemplate template, String command) {
        if (template == null) return;
        if (template.getMapperConfig() != null)
            formTemplateService.jsonConfigToMapper(template);
        Map params = new HashMap<String, Object>();
        params.put("template", template);
        params.put("command", command);
        Window window = (Window) Executions.createComponents(pages.getProperty("form.templates.process_dialog"), null, params);
        window.doModal();
    }

    @Override
    public String getDateTypeDescription(FormDateType dateType) {
        switch (dateType) {
            case CURRENT:
                return Labels.getLabel("object_date_type_current");
            case THIS_YEAR_BEGINNING:
                return Labels.getLabel("object_date_type_this_year");
            case LAST_YEAR_BEGINNING:
                return Labels.getLabel("object_date_type_last_year");
            case OFFSET:
                return Labels.getLabel("object_date_type_offset");
            case CUSTOM:
                return Labels.getLabel("object_date_type_custom");
            default:
                return "";
        }
    }

    @Override
    public String getOffsetTypeDescription(FormDateType.OffsetType offsetType) {
        switch (offsetType) {
            case DAYS:
                return Labels.getLabel("object_date_type_offset_days");
            case MONTHS:
                return Labels.getLabel("object_date_type_offset_months");
            case QUARTERS:
                return Labels.getLabel("object_date_type_offset_quarters");
            case YEARS:
                return Labels.getLabel("object_date_type_offset_years");
            default:
                return "";
        }
    }

    @Override
    public ListModelList<FormDateType> getDateTypes() {
        ListModelList<FormDateType> dateTypes = new ListModelList<>();
        dateTypes.addAll(Arrays.asList(FormDateType.values()));
        return dateTypes;
    }

    @Override
    public ListModelList<FormDateType.OffsetType> getOffsetTypes() {
        ListModelList<FormDateType.OffsetType> offsetTypes = new ListModelList<>();
        offsetTypes.addAll(Arrays.asList(FormDateType.OffsetType.values()));
        return offsetTypes;
    }

    @Override
    public void exportFile(Book book) {
        try {
            Exporter exporter = Exporters.getExporter();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            exporter.export(book, baos);
            Filedownload.save(new AMedia(String.format("%s.%s", book.getBookName(), ExcelFormat.XLSX.name().toLowerCase()), null, null,
                                         baos.toByteArray()));
        } catch (IOException e) {
            throw new CrsException(e);
        }
    }

    @Override
    public void importFile(FormTemplate formTemplate) {
        try {
            Book book = Importers.getImporter().imports(formTemplate.getBook().getContent(), "");
            List<ImportObject> importObjects = prepareFormMap(book, formTemplate.getMapper(), TemplateType.IMPORT);
            importForm(book, importObjects);
        } catch (IOException e) {
            throw new CrsException(e);
        }
    }

    @Override
    public int getMaxRows(Book book) {
        if (book == null)
            return 1;
        int maxRow = 0;
        for (int i = 0; i < book.getNumberOfSheets(); i++) {
            SSheet sSheet = book.getSheetAt(i).getInternalSheet();
            if (sSheet.getEndRowIndex() > maxRow)
                maxRow = sSheet.getEndRowIndex();
        }
        return maxRow + 1;
    }

    @Override
    public int getMaxColumns(Book book) {
        if (book == null)
            return 1;
        int maxColumn = 0;
        for (int i = 0; i < book.getNumberOfSheets(); i++) {
            SSheet sSheet = book.getSheetAt(i).getInternalSheet();
            int maxRow = sSheet.getEndRowIndex();
            for (int j = 0; j <= maxRow; j++)
                if (sSheet.getEndCellIndex(j) > maxColumn)
                    maxColumn = sSheet.getEndCellIndex(j);
        }
        return maxColumn + 1;
    }
}
