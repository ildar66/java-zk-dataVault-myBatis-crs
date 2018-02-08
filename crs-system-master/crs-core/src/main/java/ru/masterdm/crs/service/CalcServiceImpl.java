package ru.masterdm.crs.service;

import static ru.masterdm.crs.domain.calc.Calculation.CalculationAttributeMeta;
import static ru.masterdm.crs.domain.calc.Calculation.CalculationModelAttributeMeta;
import static ru.masterdm.crs.domain.calc.Model.ModelAttributeMeta;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.NumberUtils;
import org.springframework.validation.annotation.Validated;

import ru.masterdm.crs.dao.calc.CalcDao;
import ru.masterdm.crs.dao.calc.dto.FormulaResultMultiLinkDto;
import ru.masterdm.crs.dao.calc.dto.FormulaResultMultiLinkDtoReference;
import ru.masterdm.crs.dao.calc.dto.FormulaResultParameter;
import ru.masterdm.crs.dao.entity.meta.MetadataDao;
import ru.masterdm.crs.domain.AuditAction;
import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.ClassifierAttributeMeta;
import ru.masterdm.crs.domain.calc.EvalResult;
import ru.masterdm.crs.domain.calc.Formula;
import ru.masterdm.crs.domain.calc.FormulaData;
import ru.masterdm.crs.domain.calc.FormulaDependencyPair;
import ru.masterdm.crs.domain.calc.FormulaResult;
import ru.masterdm.crs.domain.calc.FormulaResultType;
import ru.masterdm.crs.domain.calc.FormulaVisitor;
import ru.masterdm.crs.domain.calc.InputFormAttributeMeta;
import ru.masterdm.crs.domain.calc.Model;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.User;
import ru.masterdm.crs.domain.entity.attribute.EntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.criteria.Conjunction;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.criteria.Where;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.criteria.WhereParenthesesGroup;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.CalculationProfileAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.domain.form.FormTemplate;
import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.exception.LockErrorCode;
import ru.masterdm.crs.exception.LockException;
import ru.masterdm.crs.exception.calc.CalculationErrorCode;
import ru.masterdm.crs.exception.calc.CalculationException;
import ru.masterdm.crs.exception.calc.FormulaCyclicDependencyException;
import ru.masterdm.crs.exception.calc.FormulaErrorCode;
import ru.masterdm.crs.exception.calc.FormulaException;
import ru.masterdm.crs.exception.calc.ModelErrorCode;
import ru.masterdm.crs.exception.calc.ModelException;
import ru.masterdm.crs.service.calc.CalculationService;
import ru.masterdm.crs.service.calc.FormulaService;
import ru.masterdm.crs.service.entity.EntityDbService;
import ru.masterdm.crs.util.annotation.Audit;
import ru.masterdm.crs.util.annotation.CurrentTimeStamp;
import ru.masterdm.crs.util.annotation.security.PreAuthorizeCopyEntity;
import ru.masterdm.crs.util.annotation.security.PreAuthorizeGetCalculation;
import ru.masterdm.crs.util.annotation.security.PreAuthorizePersistEntity;
import ru.masterdm.crs.util.annotation.security.PreAuthorizePublishEntity;
import ru.masterdm.crs.util.annotation.security.PreAuthorizeRemoveEntity;

/**
 * Calc service implementation.
 * @author Alexey Chalov
 */
@Validated
@Service("calcService")
public class CalcServiceImpl implements CalcService {

    @Autowired
    private MetadataDao metadataDao;
    @Autowired
    private CalcDao calcDao;
    @Autowired
    private FormulaService formulaService;
    @Autowired
    private EntityDbService entityDbService;
    @Autowired
    private EntityService entityService;
    @Autowired
    private EntityMetaService entityMetaService;
    @Autowired
    private LockService lockService;
    @Autowired
    private CalculationService calculationService;
    @Autowired
    private FormTemplateService formTemplateService;
    @Autowired
    private SecurityService securityService;

    @Value("#{config['formula.script.eval.maxtime']}")
    private long scriptEvalTimeout;
    @Value("#{config['formula.variable.regexp']}")
    private String formulaVarRegexp;
    @Value("#{config['model.publish.lock.waitTimeout']}")
    private Integer modelPublishLockWaitTimeout;

    private Pattern formulaVarPattern;

    private static final Logger LOG = LoggerFactory.getLogger(CalcServiceImpl.class);

    @Override
    @Audit(action = AuditAction.CREATE_FORMULA)
    @Transactional
    public void persistFormula(@NotNull Formula formula) {
        if (formula.getFormula() == null) {
            formula.setFormula(new FormulaData());
        }

        Set<String> attributeNames = new HashSet<>();
        formula.getChildren().stream()
               .filter(p -> !p.getRight().isLibrary())
               .forEach(p -> {
                   if (!formulaVarPattern.matcher(p.getLeft()).matches()) {
                       throw new FormulaException(FormulaErrorCode.VARIABLE_INVALID_NAME);
                   }
                   if (!attributeNames.contains(p.getLeft())) {
                       attributeNames.add(p.getLeft());
                   } else {
                       throw new FormulaException(FormulaErrorCode.DUPLICATE_ATTRIBUTE_NAME);
                   }
                   if (formula.isLibrary()) {
                       throw new FormulaException(FormulaErrorCode.LIBRARY_INVALID_CHILDREN);
                   }
               });
        if (formula.getChildren().stream()
                   .filter(p -> (p.getRight().isLibrary() && !formula.getEvalLang().equalsIgnoreCase(p.getRight().getEvalLang())))
                   .count() > 0) {
            throw new FormulaException(FormulaErrorCode.MULTIPLE_LIBRARY_EVAL_LANGS);
        }

        formula.setDigest(formula.calcDigest());
        formula.getFormula().setDigest(formula.getFormula().calcDigest());
        try {
            calcDao.persistFormula(formula);
        } catch (UncategorizedSQLException e) {
            if (e.getSQLException().getErrorCode() == FormulaErrorCode.CYCLIC_DEPENDENCY.getCode()) {
                throw new FormulaCyclicDependencyException(calcDao.getCyclicDependencyFormulas(), e);
            }
            throw e;
        }
    }

    @Override
    @Audit(action = AuditAction.REMOVE_FORMULA)
    @Transactional
    public void removeFormula(@NotNull Formula formula) {
        calcDao.removeFormula(formula);
        formula.getChildren().clear();
    }

    @Override
    @Transactional(readOnly = true)
    public Formula getFormulaByKey(@NotNull String key, @CurrentTimeStamp LocalDateTime ldts) {
        return calcDao.getFormulaByKey(key, ldts);
    }

    @Override
    public List<Formula> getFormulas(Criteria criteria, @CurrentTimeStamp LocalDateTime ldts) {
        return calcDao.getFormulas(criteria, ldts);
    }

    /**
     * Evaluates single formula.
     * @param formula {@link Formula} instance
     * @param attrs map of formula attributes
     * @return {@link EvalResult} instance
     */
    @Override
    public EvalResult eval(@NotNull Formula formula, Map<String, Object> attrs) {
        StringWriter output = new StringWriter();
        SimpleScriptContext ctx = new SimpleScriptContext();
        ctx.setWriter(output);
        if (attrs != null) {
            attrs.keySet().forEach(attr -> ctx.setAttribute(attr, attrs.get(attr), ScriptContext.ENGINE_SCOPE));
        }
        try {
            Future<Object> future = formulaService.eval(formula, ctx);
            Object result = future.get(scriptEvalTimeout, TimeUnit.SECONDS);
            return new EvalResult(result, ctx.getWriter().toString(), null);
        } catch (Exception e) {
            return new EvalResult(null, ctx.getWriter().toString(), e);
        }
    }

    @Async
    @Transactional
    @Audit(action = AuditAction.EVAL_CALCULATION)
    @Override
    public Future<Void> eval(@NotNull Calculation calculation) {
        if (calcDao.isCalculationPublished(calculation.getId())) {
            LOG.error(String.format("Calculation (satellite id: %s, key: '%s') is published already", calculation.getId(), calculation.getKey()));
            throw new CalculationException(CalculationErrorCode.CALCULATE_PUBLISHED);
        }

        try {
            lockService.lock(calculation);

            calculation.getProfiles().stream().forEach(profile -> {
                List<Formula> formulas = calculation.getModel().getFormulas();
                List<Formula> treeFormulas = new ArrayList<>();
                formulas.stream().parallel().forEach(formula -> {
                /* enriches formula tree */
                    treeFormulas.add(formulaService.getFormulaTree(formula, calculation.getModel().getActuality()));
                });

                /* set commonResults variable to empty map if need to suppress common formula optimization mechanism */
                Map<String, EvalResult> commonResults = getCommonFormulaResults(treeFormulas, calculation, profile);

                treeFormulas.stream().parallel().forEach(formula -> {
                    Map<Pair<String, Formula>, EvalResult> evaluatedResults = new HashMap<>();
                    for (Pair<String, Formula> pair : buildCalculationOrder(formula)) {
                        if (hasFailedEvalChildren(pair, evaluatedResults)) {
                            saveEvaluationResults(calculation, profile, pair.getRight(), new EvalResult(null, null, null));
                            continue;
                        }
                        EvalResult result = commonResults.get(pair.getRight().getKey());
                        if (result == null) {
                            result = eval(pair.getRight(),
                                          formulaService.buildBindings(buildFormulaAttributes(pair, evaluatedResults), calculation, profile));
                            if (LOG.isDebugEnabled()) {
                                LOG.debug(
                                        "\n#eval  (" + profile.getKey() + ")---------------------------------------------\n"
                                        + "Formula key: " + pair.getRight().getKey() + "\n"
                                        + "Formula data: \n" + pair.getRight().getFormula().getData() + "\n"
                                        + "Result: '" + result.getResult() + "'\n"
                                        + "Output: \n" + result.getOutput().trim() + "\n"
                                        + "Exception: \n" + result.getException() + "\n"
                                        + "--------------------------------------------------------------------------\n");
                            }
                            if (!pair.getRight().isLibrary()) {
                                saveEvaluationResults(calculation, profile, pair.getRight(), result);
                            }
                            if (result.getException() != null) {
                                LOG.warn("Error evaluating formula.", result.getException());
                            }
                        }
                        evaluatedResults.put(pair, result);
                    }
                });
            });

            calculation.setCalculated(true);
            persistCalculation(calculation);

        } catch (LockException e) {
            if (e.getErrorCode() != LockErrorCode.TIMEOUT) // lock hold by others
                throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } finally {
            lockService.unlock(calculation);
        }

        return new AsyncResult<>(null);
    }

    /**
     * Returns true, if there are more than zero evaluation failed child formulas, false otherwise.
     * @param pair pair to test
     * @param evaluatedResults already evaluated results
     * @return boolean
     */
    private boolean hasFailedEvalChildren(Pair<String, Formula> pair,
                                          Map<Pair<String, Formula>, EvalResult> evaluatedResults) {
        List<Pair<String, Formula>> failedFormulas =
            evaluatedResults.entrySet().stream()
                            .filter(k -> k.getValue().getException() != null)
                            .map(k -> k.getKey()).collect(Collectors.toList());
        if (failedFormulas.isEmpty()) {
            return false;
        }
        if (failedFormulas.contains(pair)) {
            return true;
        }
        Set<Pair<String, Formula>> allChildren = new HashSet<>();
        collectAllChildren(pair.getRight().getChildren(), allChildren);
        return CollectionUtils.containsAny(allChildren, failedFormulas);
    }

    /**
     * Collects all children pairs starting with passed ones.
     * @param pairs starting pairs
     * @param result aggregate result holder
     */
    private void collectAllChildren(List<? extends Pair<String, Formula>> pairs, final Set<Pair<String, Formula>> result) {
        pairs.stream().forEach(p -> {
            result.add(p);
            collectAllChildren(p.getRight().getChildren(), result);
        });
    }

    /**
     * Save evaluation results.
     * @param calculation calculation object
     * @param profile calculation profile
     * @param formula formula object
     * @param evalResult formula evaluation result
     */
    private void saveEvaluationResults(Calculation calculation, Entity profile, Formula formula, EvalResult evalResult) {
        FormulaResult formulaResult = (FormulaResult) entityService.newEmptyEntity(FormulaResult.METADATA_KEY);
        if (formula.getResultType() == FormulaResultType.NUMBER) {
            Number n = (Number) evalResult.getResult();
            if (n != null)
                formulaResult.setNumberResult(NumberUtils.convertNumberToTargetClass(n, BigDecimal.class));
        } else {
            formulaResult.setStringResult((String) evalResult.getResult());
        }

        if (evalResult.getException() != null)
            formulaResult.setException(ExceptionUtils.getStackTrace(evalResult.getException()));
        formulaResult.setOutput(evalResult.getOutput());
        formula.setFormulaResult(formulaResult, calculation, profile);

        calculationService.persistsFormulaResultsAndTrinityLinks(calculation, formula, profile, metadataDao.getSysTimestamp());
    }

    @Override
    @Transactional
    public List<Formula> getFormulaTrees(Criteria criteria, @CurrentTimeStamp LocalDateTime ldts) {
        return formulaService.getFormulaTrees(criteria, null, ldts);
    }

    @Override
    @Transactional
    public List<Formula> getFormulaTrees(Criteria criteria, @NotNull Calculation calculation, @CurrentTimeStamp LocalDateTime ldts) {
        return formulaService.getFormulaTrees(criteria, calculation, ldts);
    }

    @Override
    public boolean isFormulaExists(@NotNull String key) {
        return calcDao.getFormulaIdByKey(key) != null;
    }

    @Override
    public List<Model> getModels(Criteria criteria, RowRange rowRange, @CurrentTimeStamp LocalDateTime ldts) {
        return getModels(criteria, rowRange, ldts, false);
    }

    /**
     * Get model list using flag to get it by id or id-ldts.
     * @param criteria query criteria
     * @param rowRange paging object
     * @param ldts load datetime
     * @param useModelLdts td/id-ldta manage flag
     * @return list of models
     */
    private List<Model> getModels(Criteria criteria, RowRange rowRange, @CurrentTimeStamp LocalDateTime ldts, boolean useModelLdts) {
        EntityMeta entityMeta = getModelEntityMetadata(ldts);
        List<Model> modelList = (List<Model>) entityService.getEntitiesBase(entityMeta, criteria, rowRange, ldts);
        if (!modelList.isEmpty())
            readModelReferences(entityMeta, modelList, useModelLdts, ldts);
        return modelList;
    }

    @Transactional
    @Audit(action = AuditAction.CREATE_MODEL)
    @Override
    public void persistModel(@NotNull Model model) {
        EntityMeta entityMeta = model.getMeta();

        if (model.getId() != null) {
            if (calcDao.isModelPublished(model.getHubId(), model.getLdts())) {
                boolean setChanged = false;

                Criteria criteria = new Criteria();
                criteria.getWhere().addItem(new WhereItem(entityMeta.getKeyAttribute(), Operator.EQ, model.getKey()));
                Model storedModel = getModels(criteria, null, model.getLdts(), true).get(0);

                if (storedModel != null) {
                    boolean classifiersChanged = isSetsChanged(storedModel.getClassifiers(), model.getClassifiers());
                    boolean formsChanged = isSetsChanged(storedModel.getInputForms(), model.getInputForms())
                                           || isFormPeriodsChanged(storedModel, model);
                    boolean formulasChanged = isSetsChanged(storedModel.getFormulas(), model.getFormulas());
                    boolean formTemplateChanged = isSetsChanged(storedModel.getFormTemplates(), model.getFormTemplates());
                    setChanged = classifiersChanged || formsChanged || formulasChanged || formTemplateChanged
                                 || !model.getDigest().equals(model.calcDigest());
                    if (setChanged) {
                        model.setPublished(false);
                        model.setVersion(null);
                    }
                }
            }
        }

        LocalDateTime ldts = metadataDao.getSysTimestamp();
        entityService.persistEntityBase(model, ldts);

        LinkedEntityAttribute formulas = ((LinkedEntityAttribute) model.getAttribute(ModelAttributeMeta.FORMULAS.getKey()));
        entityDbService.mergeLink(model, formulas.getEntityAttributeList(), formulas.getMeta(), ldts);

        LinkedEntityAttribute<Entity> inputForms = ((LinkedEntityAttribute) model.getAttribute(ModelAttributeMeta.INPUT_FORMS.getKey()));
        entityDbService.mergeLink(model, inputForms.getEntityAttributeList(), inputForms.getMeta(), ldts);
        for (EntityAttribute<Entity> ea : inputForms.getEntityAttributeList())
            entityDbService.writeReferenceLinkSatellite(model, ea, ldts);

        LinkedEntityAttribute<Entity> classifiers = ((LinkedEntityAttribute) model.getAttribute(ModelAttributeMeta.CLASSIFIERS.getKey()));
        entityDbService.mergeLink(model, classifiers.getEntityAttributeList(), classifiers.getMeta(), ldts);

        LinkedEntityAttribute<FormTemplate> formTemplates = ((LinkedEntityAttribute) model.getAttribute(ModelAttributeMeta.FORM_TEMPLATES.getKey()));
        entityDbService.mergeLink(model, formTemplates.getEntityAttributeList(), formTemplates.getMeta(), ldts);
    }

    @Transactional
    @Audit(action = AuditAction.PUBLISH_MODEL)
    @Override
    public void publishModel(@NotNull Model model) {
        try {
            lockService.lock(model, modelPublishLockWaitTimeout == null ? 0 : modelPublishLockWaitTimeout);

            Long version = calcDao.getModelLastVersion(model.getKey(), metadataDao.getSysTimestamp());
            if (version == null || version.equals(0L))
                version = 1L;
            else
                version += 1;

            model.setPublished(true);
            model.setVersion(version);
            persistModel(model);
        } finally {
            lockService.unlock(model);
        }
    }

    @Transactional
    @Audit(action = AuditAction.REMOVE_MODEL)
    @Override
    public void removeModel(@NotNull Model model) {
        if (calcDao.isModelPublished(model.getHubId(), metadataDao.getSysTimestamp()))
            throw new ModelException(ModelErrorCode.REMOVE_PUBLISHED);

        entityService.removeEntity(model);
    }

    @Override
    public List<Model> getPublishedModels(Criteria criteria, RowRange rowRange, @CurrentTimeStamp LocalDateTime ldts) {
        WhereItem whereItem = new WhereItem(getModelEntityMetadata(ldts).getAttributeMetadata(ModelAttributeMeta.PUBLISHED.getKey()),
                                            Operator.EQ,
                                            Boolean.TRUE);
        return getModels(getEnrichedCriteria(criteria, false, whereItem), rowRange, ldts, true);
    }

    @Override
    public List<Model> getDraftModels(Criteria criteria, RowRange rowRange, @CurrentTimeStamp LocalDateTime ldts) {
        WhereItem whereItem = new WhereItem(getModelEntityMetadata(ldts).getAttributeMetadata(ModelAttributeMeta.PUBLISHED.getKey()),
                                            Operator.EQ,
                                            Boolean.FALSE);
        return getModels(getEnrichedCriteria(criteria, true, whereItem), rowRange, ldts);
    }

    @Override
    @PreAuthorizeGetCalculation
    public List<Calculation> getCalculations(Criteria criteria, RowRange rowRange, @CurrentTimeStamp LocalDateTime ldts) {
        if (criteria == null)
            criteria = new Criteria();
        criteria.setUser(securityService.getCurrentUser());
        EntityMeta entityMeta = getCalculationEntityMetadata(ldts);
        List<Calculation> calculationList = (List<Calculation>) entityService.getEntitiesBase(entityMeta, criteria, rowRange, ldts);
        if (!calculationList.isEmpty())
            readCalculationReferences(entityMeta, calculationList, ldts);
        return calculationList;
    }

    @Override
    @PreAuthorizeGetCalculation
    public List<Calculation> getPublishedCalculations(Criteria criteria, RowRange rowRange, @CurrentTimeStamp LocalDateTime ldts) {
        WhereItem whereItem = new WhereItem(getCalculationEntityMetadata(ldts).getAttributeMetadata(CalculationAttributeMeta.PUBLISHED.getKey()),
                                            Operator.EQ,
                                            Boolean.TRUE);
        return getCalculations(getEnrichedCriteria(criteria, false, whereItem), rowRange, ldts);
    }

    @Override
    @PreAuthorizeGetCalculation
    public List<Calculation> getDraftCalculations(Criteria criteria, RowRange rowRange, @CurrentTimeStamp LocalDateTime ldts) {
        WhereItem whereItem = new WhereItem(getCalculationEntityMetadata(ldts).getAttributeMetadata(CalculationAttributeMeta.PUBLISHED.getKey()),
                                            Operator.EQ,
                                            Boolean.FALSE);
        return getCalculations(getEnrichedCriteria(criteria, true, whereItem), rowRange, ldts);
    }

    @Override
    public boolean isCalculationHasException(Calculation calculation) {
        return calculation.getModel().getFormulas()
                          .stream()
                          .flatMap(formula -> calculation.getProfiles()
                                                         .stream()
                                                         .map(profile -> new AbstractMap.SimpleEntry<>(formula, profile)))
                          .anyMatch(e -> (e.getKey().getFormulaResult(calculation, e.getValue()) == null)
                                         || (e.getKey().getFormulaResult(calculation, e.getValue()).getException() != null)
                          );
    }

    @Transactional
    @Audit(action = AuditAction.PUBLISH_CALCULATION)
    @Override
    @PreAuthorizePublishEntity
    public void publishCalculation(@NotNull @P("entity") Calculation calculation) {
        CalculationErrorCode calculationErrorCode = checkCalculationForPublish(calculation);
        if (calculationErrorCode != null)
            throw new CalculationException(calculationErrorCode);

        Criteria criteria = new Criteria();
        LocalDateTime sysTimestamp = metadataDao.getSysTimestamp();
        Model model = calculation.getModel();
        EntityMeta modelMeta = entityMetaService.getEntityMetaByKey(Model.METADATA_KEY, sysTimestamp);
        criteria.getWhere().addItem(new WhereItem(modelMeta.getKeyAttribute(), Operator.EQ, model.getKey()));
        List<Model> models = (List<Model>) entityService.getEntitiesBase(modelMeta, criteria, null, sysTimestamp);
        Long modelVersion = (model.getVersion() == null ? Long.valueOf(0L) : model.getVersion());
        Long latestModelVersion = (models.get(0).getVersion() == null ? Long.valueOf(0L) : models.get(0).getVersion());
        if (latestModelVersion.compareTo(modelVersion) > 0)
            throw new CalculationException(CalculationErrorCode.PUBLISH_WITH_OBSOLETE_MODEL);

        calculation.setPublished(true);
        persistCalculation(calculation);
    }

    @Transactional
    @Audit(action = AuditAction.CREATE_CALCULATION)
    @Override
    @PreAuthorizePersistEntity
    public void persistCalculation(@NotNull @P("entity") Calculation calculation) {
        if (calculation.getId() != null && calcDao.isCalculationPublished(calculation.getId()))
            throw new CalculationException(CalculationErrorCode.PERSIST_PUBLISHED);

        LocalDateTime ldts = metadataDao.getSysTimestamp();
        // TODO try to implement check setPublished call authority by API for Privileged Blocks calculation.setPublished(false);
        if (calculation.getId() != null && calculation.isCalculated() && isCalculationFlagReset(calculation, ldts))
            calculation.setCalculated(false);
        fillCalculationClients(calculation, ldts);

        entityService.persistEntityBase(calculation, ldts);

        LinkedEntityAttribute authors = ((LinkedEntityAttribute) calculation.getAttribute(CalculationAttributeMeta.AUTHOR.getKey()));
        entityDbService.mergeLink(calculation, authors.getEntityAttributeList(), authors.getMeta(), ldts);

        // write MODEL link and attribute
        LinkedEntityAttribute<Model> models = ((LinkedEntityAttribute) calculation.getAttribute(CalculationAttributeMeta.MODEL.getKey()));
        entityDbService.mergeLink(calculation, models.getEntityAttributeList(), models.getMeta(), ldts);
        if (!models.isEmpty()) {
            EntityAttribute ea = models.getEntityAttributeList().get(0);
            if (calculation.getModel().getVersion() != null) {
                ea.getSatellite()
                  .setAttributeValue(CalculationModelAttributeMeta.VERSION.getKey(), new BigDecimal(calculation.getModel().getVersion()));
            }
            entityDbService.writeReferenceLinkSatellite(calculation, ea, ldts);
        }

        LinkedEntityAttribute<Entity> clients = ((LinkedEntityAttribute) calculation.getAttribute(CalculationAttributeMeta.CLIENT.getKey()));
        entityDbService.mergeLink(calculation, clients.getEntityAttributeList(), clients.getMeta(), ldts);
        for (EntityAttribute<Entity> ea : clients.getEntityAttributeList())
            entityDbService.writeReferenceLinkSatellite(calculation, ea, ldts);

        LinkedEntityAttribute clientGroups = ((LinkedEntityAttribute) calculation.getAttribute(CalculationAttributeMeta.CLIENT_GROUP.getKey()));
        entityDbService.mergeLink(calculation, clientGroups.getEntityAttributeList(), clientGroups.getMeta(), ldts);

        LinkedEntityAttribute profiles = ((LinkedEntityAttribute) calculation.getAttribute(CalculationAttributeMeta.CALC_PROFILE.getKey()));
        entityDbService.mergeLink(calculation, profiles.getEntityAttributeList(), profiles.getMeta(), ldts);

        securityService.pendingSecureChange(Calculation.METADATA_KEY, calculation);
    }

    /**
     * Checks calculation for publish.
     * @param calculation calculation
     * @return Exception code if exists
     */
    private CalculationErrorCode checkCalculationForPublish(Calculation calculation) {
        if (!calcDao.isCalculationCalculated(calculation.getId()))
            return CalculationErrorCode.PUBLISH_NOT_CALCULATED;
        if (calculation.getClient() == null && calculation.getClientGroup() == null)
            return CalculationErrorCode.PUBLISH_WITHOUT_CLIENT_OR_GROUP;
        if (isCalculationHasException(calculation))
            return CalculationErrorCode.PUBLISH_WITH_EXCEPTION;
        return null;
    }

    /**
     * Check whether calculated flag must be reset.
     * @param calculation calculation
     * @param ldts load date
     * @return true if calcualtion flag must be reset, false otherwise
     */
    private boolean isCalculationFlagReset(Calculation calculation, LocalDateTime ldts) {
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(calculation.getMeta().getHubIdAttribute(), Operator.EQ, calculation.getHubId()));
        EntityMeta calcEntityMeta = getCalculationEntityMetadata(ldts);
        criteria.setUser(securityService.getCurrentUser());
        List<Calculation> calculationList = (List<Calculation>) entityService.getEntitiesBase(calcEntityMeta, criteria, null, ldts);
        entityDbService.readEntityAttributes(calculationList, ldts, false, null);
        Calculation storedCalculation = calculationList.get(0);

        boolean actualitySame = Objects.equals(storedCalculation.getActuality(), calculation.getActuality());
        boolean dataActualitySame = Objects.equals(storedCalculation.getDataActuality(), calculation.getDataActuality());
        boolean modelSame = Objects.equals(getAttributeLinkedHubId(calculation, CalculationAttributeMeta.MODEL.getKey()),
                                           getAttributeLinkedHubId(storedCalculation, CalculationAttributeMeta.MODEL.getKey()));
        Long clientGroupHubId = getAttributeLinkedHubId(calculation, CalculationAttributeMeta.CLIENT_GROUP.getKey());
        boolean clientGroupSame = Objects.equals(getAttributeLinkedHubId(storedCalculation, CalculationAttributeMeta.CLIENT_GROUP.getKey()),
                                                 clientGroupHubId);
        boolean clientSame = true;
        if (clientGroupHubId == null && clientGroupSame) {
            clientSame = Objects.equals(getAttributeLinkedHubId(calculation, CalculationAttributeMeta.CLIENT.getKey()),
                                        getAttributeLinkedHubId(storedCalculation, CalculationAttributeMeta.CLIENT.getKey()));
        }
        return !modelSame || !clientSame || !clientGroupSame || !actualitySame || !dataActualitySame;
    }

    /**
     * Returns attribute linked hub id.
     * @param entity entity
     * @param attributeKey attribute key
     * @return attribute linked hub id
     */
    private Long getAttributeLinkedHubId(Entity entity, String attributeKey) {
        AttributeMeta referenceMeta = entity.getMeta().getAttributeMetadata(attributeKey);
        List<EntityAttribute<Entity>> attributes = ((LinkedEntityAttribute<Entity>) entity.getAttribute(referenceMeta.getKey()))
                .getEntityAttributeList();
        return (!attributes.isEmpty()) ? attributes.get(0).getLinkedHubId() : null;
    }

    /**
     * Fills calculation clients if client group have been set.
     * @param calculation calculation
     * @param ldts load time
     */
    private void fillCalculationClients(Calculation calculation, LocalDateTime ldts) {
        if (calculation.getClientGroup() == null)
            return;
        Calculation sourceCalculation = null;
        if (calculation.getId() == null && calculation.isParentExists()) {
            if (calculation.getParentReferenceAttribute().getEntityList().isEmpty())
                entityService.loadEntityParent(calculation, ldts);
            sourceCalculation = (Calculation) calculation.getParentReferenceAttribute().getEntityList().get(0);
        }
        LinkedEntityAttribute clientAttribute = (LinkedEntityAttribute) calculation.getAttribute(CalculationAttributeMeta.CLIENT.getKey());
        if (sourceCalculation != null && sourceCalculation.getClientGroup() != null
            && sourceCalculation.getClientGroup().getHubId().equals(calculation.getClientGroup().getHubId())) {
            LinkedEntityAttribute sourceAttribute = (LinkedEntityAttribute) sourceCalculation.getAttribute(CalculationAttributeMeta.CLIENT.getKey());
            List<Entity> sourceClients = new ArrayList(sourceAttribute.getEntityList());
            List wipedEntityAttributeList = new ArrayList(sourceAttribute.getEntityAttributeList());
            wipeEntityAttributeIdentifiers(wipedEntityAttributeList);
            sourceAttribute.getEntityList().clear();
            sourceAttribute.getEntityAttributeList().clear();
            entityDbService.readEntityAttributes(Collections.singletonList(sourceCalculation), ldts, false,
                                                 (am) -> am.getKey().equals(CalculationAttributeMeta.CLIENT.getKey()));
            List<EntityAttribute> sourceEntityAttributeList = sourceAttribute.getEntityAttributeList();
            for (EntityAttribute ea : sourceEntityAttributeList) {
                Entity entity = sourceClients.stream().filter(e -> e.getHubId().equals(ea.getLinkedHubId())).findFirst().orElse(null);
                ea.setEntity(entity);
            }
            clientAttribute.getEntityAttributeList().clear();
            clientAttribute.getEntityAttributeList().addAll(wipedEntityAttributeList);
        } else {
            LinkedEntityAttribute clientGroupAttribute
                    = (LinkedEntityAttribute) calculation.getAttribute(CalculationAttributeMeta.CLIENT_GROUP.getKey());
            List<EntityAttribute<Entity>> entityAttributeList = clientGroupAttribute.getEntityAttributeList();
            boolean groupChanged = entityAttributeList.stream().anyMatch(ea -> ea.getLinkLdts() == null);
            if (groupChanged) {
                EntityMeta clientMeta = entityMetaService.getEntityMetaByKey(ClientAttributeMeta.METADATA_KEY, ldts);
                Criteria criteria = new Criteria();
                criteria.addReferencedEntity(calculation.getClientGroup());
                List<? extends Entity> clientsToSet = entityService.getEntities(clientMeta, criteria, null, null);
                List clients = clientAttribute.getEntityList();
                clients.clear();
                clients.addAll(clientsToSet);
            }
        }
    }

    @Transactional
    @Audit(action = AuditAction.COPY_CALCULATION)
    @Override
    @PreAuthorizeCopyEntity
    public void copyCalculation(@NotNull @P("entity") Calculation calculation) {
        if (calculation.getId() != null)
            throw new CrsException("calculation id is not null");
        if (calculation.getClient() != null && calculation.getClientGroup() != null)
            throw new CrsException("client and client group are both not empty");
        if (!calculation.isParentExists() || calculation.getParentReferenceAttribute().getEntityList().isEmpty())
            throw new CrsException("parent calculation is empty");
        if (calculation.getModel() == null)
            throw new CrsException("model is empty");
        if (calculation.getProfiles().isEmpty())
            throw new CrsException("profiles are empty");
        Calculation sourceCalculation = (Calculation) calculation.getParentReferenceAttribute().getEntityList().get(0);
        if (sourceCalculation.getModel() == null)
            throw new CrsException("source model is empty");
        Set<Long> sourceCalculationProfileHubIds = sourceCalculation.getProfiles().stream().map(Entity::getHubId).collect(Collectors.toSet());
        Set<String> intersectedProfileKeys = calculation.getProfiles().stream().filter(e -> sourceCalculationProfileHubIds.contains(e.getHubId()))
                                                        .map(Entity::getKey).collect(Collectors.toSet());
        if (intersectedProfileKeys.isEmpty())
            return;
        LocalDateTime now = entityMetaService.getSysTimestamp();
        LinkedEntityAttribute<User> linkedUsers = (LinkedEntityAttribute<User>) calculation.getAttribute(CalculationAttributeMeta.AUTHOR.getKey());
        linkedUsers.getEntityList().clear();
        linkedUsers.add(securityService.getCurrentUser());
        calculation.setPublished(false);
        calculation.setCalculated(false);

        persistCalculation(calculation);
        entityDbService.writeReferenceNewAndChange(calculation, calculation.getParentReferenceAttribute(), now);

        boolean copyToOtherProfiles = intersectedProfileKeys.size() == 1 && calculation.getProfiles().size() > 1;
        copyClassifierValues(sourceCalculation, calculation, intersectedProfileKeys, now, copyToOtherProfiles);
        copyInputFormValues(sourceCalculation, calculation, intersectedProfileKeys, now, copyToOtherProfiles);
    }

    /**
     * Copies input form values to copied calculation.
     * @param sourceCalculation source calculation
     * @param copiedCalculation copied calculation
     * @param intersectedProfileKeys intersected profile keys
     * @param now current load time
     * @param copyToAllProfiles is need copy to other profiles (for example RATED copied to RATED and EXPERT).
     * true if need copy to all profiles, false otherwise
     */
    private void copyInputFormValues(Calculation sourceCalculation, Calculation copiedCalculation, Set<String> intersectedProfileKeys,
                                     LocalDateTime now, boolean copyToAllProfiles) {
        LocalDateTime actuality = copiedCalculation.getModel().getActuality();
        Set<Long> inputFormHubIds = formTemplateService.getInputForms(copiedCalculation.getModel().getFormTemplates())
                                                       .stream().map(e -> e.getHubId()).collect(Collectors.toSet());
        List<EntityMeta> inputForms = formTemplateService.getInputForms(sourceCalculation.getModel().getFormTemplates())
                                                         .stream().filter(inputForm -> inputFormHubIds.contains(inputForm.getHubId()))
                                                         .collect(Collectors.toList());
        EntityMeta profileMeta = entityMetaService.getEntityMetaByKey(CalculationProfileAttributeMeta.METADATA_KEY, actuality);
        Object[] intersectedProfileArray = intersectedProfileKeys.toArray(new String[intersectedProfileKeys.size()]);
        for (EntityMeta inputForm : inputForms) {
            Criteria criteria = new Criteria();
            String profileAttrKey = entityMetaService.getAttributeMetaKey(inputForm, InputFormAttributeMeta.CALC_PROFILE.name());
            AttributeMeta profileRefAttr = inputForm.getAttributeMetadata(profileAttrKey);
            criteria.getWhere().addReferenceItem(profileRefAttr, new WhereItem(profileMeta.getKeyAttribute(), Operator.IN, intersectedProfileArray));
            List<Entity> inputFormValues = getInputFormValues(sourceCalculation, inputForm, criteria, null, now);

            if (copyToAllProfiles) {
                for (Entity profile : copiedCalculation.getProfiles()) {
                    inputFormValues.forEach(formValue -> {
                        List<Entity> profiles = ((LinkedEntityAttribute<Entity>) formValue.getAttribute(profileAttrKey)).getEntityList();
                        profiles.clear();
                        profiles.add(profile);
                    });
                    wipeEntityIdentifiers(inputFormValues, copiedCalculation);
                    persistsInputFormValues(copiedCalculation, inputForm, inputFormValues);
                }
            } else {
                wipeEntityIdentifiers(inputFormValues, copiedCalculation);
                persistsInputFormValues(copiedCalculation, inputForm, inputFormValues);
            }
        }
    }

    /**
     * Copies classifier values to copied calculation.
     * @param sourceCalculation source calculation
     * @param copiedCalculation copied calculation
     * @param intersectedProfileKeys intersected profile keys
     * @param now current load time
     * @param copyToAllProfiles is need copy to other profiles (for example RATED copied to RATED and EXPERT).
     * true if need copy to all profiles, false otherwise
     */
    private void copyClassifierValues(Calculation sourceCalculation, Calculation copiedCalculation, Set<String> intersectedProfileKeys,
                                      LocalDateTime now, boolean copyToAllProfiles) {
        List<Entity> classifierValues = getClassifierValues(sourceCalculation, now);
        Set<Long> classifierHubIds = copiedCalculation.getModel().getClassifiers().stream().map(e -> e.getHubId()).collect(Collectors.toSet());
        classifierValues = classifierValues.stream().filter(cv -> {
            String attributeMetaKey = entityMetaService.getAttributeMetaKey(cv.getMeta(), ClassifierAttributeMeta.CALC_PROFILE.name());
            List<Entity> entities = ((LinkedEntityAttribute) cv.getAttribute(attributeMetaKey)).getEntityList();
            return classifierHubIds.contains(cv.getMeta().getHubId())
                   && entities.stream().anyMatch(e -> intersectedProfileKeys.contains(e.getKey()));
        }).collect(Collectors.toList());

        if (copyToAllProfiles) {
            for (Entity profile : copiedCalculation.getProfiles()) {
                classifierValues.forEach(cv -> {
                    String attributeMetaKey = entityMetaService.getAttributeMetaKey(cv.getMeta(), ClassifierAttributeMeta.CALC_PROFILE.name());
                    List<Entity> profiles = ((LinkedEntityAttribute<Entity>) cv.getAttribute(attributeMetaKey)).getEntityList();
                    profiles.clear();
                    profiles.add(profile);
                });
                wipeEntityIdentifiers(classifierValues, copiedCalculation);
                persistsClassifierValues(copiedCalculation, classifierValues);
            }
        } else {
            wipeEntityIdentifiers(classifierValues, copiedCalculation);
            persistsClassifierValues(copiedCalculation, classifierValues);
        }
    }

    /**
     * Wipe entity identifiers to persist same entity into database as new.
     * @param entities entities
     * @param calculation calculation
     */
    private void wipeEntityIdentifiers(List<Entity> entities, Calculation calculation) {
        for (Entity entity : entities) {
            entity.setKey(null);
            entity.setId(null);
            entity.setHubId(null);
            entity.setLdts(null);
            entity.setDigest(null);

            LinkedEntityAttribute calcAttr = (LinkedEntityAttribute) entity
                    .getAttribute(entityMetaService.getAttributeMetaKey(entity.getMeta(), InputFormAttributeMeta.CALC.name()));
            calcAttr.getEntityList().clear();
            calcAttr.add(calculation);

            wipeLinkAttributeIdentifiers(entity);
        }
    }

    /**
     * Clear link attributes.
     * @param entity entity
     */
    private void wipeLinkAttributeIdentifiers(Entity entity) {
        entity.getAttributes().values().stream()
              .filter(e -> e.getMeta().getType() == AttributeType.REFERENCE)
              .flatMap(e -> ((LinkedEntityAttribute<Entity>) e).getEntityAttributeList().stream()).forEach(linkedEntity -> {
            linkedEntity.setLinkId(null);
            linkedEntity.setLinkLdts(null);
            linkedEntity.setMainHubId(null);
        });
    }

    /**
     * Clear entity attribute identifiers.
     * @param entityAttributeList entity attribute list
     */
    private void wipeEntityAttributeIdentifiers(List<EntityAttribute<Entity>> entityAttributeList) {
        for (EntityAttribute<Entity> entityAttribute : entityAttributeList) {
            entityAttribute.setLinkId(null);
            entityAttribute.setLinkLdts(null);
            entityAttribute.setMainHubId(null);
            if (entityAttribute.isSatelliteDefined()) {
                EntityAttribute<Entity>.Satellite satellite = entityAttribute.getSatellite();
                satellite.setId(null);
                satellite.setLdts(null);
                satellite.setDigest(null);
            }
        }
    }

    @Transactional
    @Audit(action = AuditAction.REMOVE_CALCULATION)
    @Override
    @PreAuthorizeRemoveEntity
    public void removeCalculation(@NotNull @P("entity") Calculation calculation) {
        if (calcDao.isCalculationPublished(calculation.getId())) {
            throw new CalculationException(CalculationErrorCode.REMOVE_PUBLISHED);
        }

        entityService.removeEntity(calculation);
    }

    @Override
    public List<FormulaResult> getFormulaResults(Criteria criteria, RowRange rowRange, @CurrentTimeStamp LocalDateTime ldts) {
        return formulaService.getFormulaResults(criteria, rowRange, ldts);
    }

    @Override
    public List<Entity> getClassifierValues(@NotNull Calculation calculation, @CurrentTimeStamp LocalDateTime ldts) {
        return calculationService.getClassifierValues(calculation, ldts);
    }

    @Override
    public void persistsClassifierValues(@NotNull Calculation calculation, @NotNull List<Entity> classifierValues) {
        if (calculation.getId() != null && calcDao.isCalculationPublished(calculation.getId())) {
            throw new CalculationException(CalculationErrorCode.PERSIST_PUBLISHED);
        }
        calculationService.persistsClassifierValues(calculation, classifierValues);
    }

    @Override
    public List<Entity> getInputFormValues(@NotNull Calculation calculation, @NotNull EntityMeta inputForm, Criteria criteria, RowRange rowRange,
                                           @CurrentTimeStamp LocalDateTime ldts) {
        return calculationService.getInputFormValues(calculation, inputForm, criteria, rowRange, ldts);
    }

    @Override
    @Transactional
    public void persistsInputFormValues(@NotNull Calculation calculation, @NotNull EntityMeta inputForm, @NotNull List<Entity> inputFormValues) {
        if (calculation.getId() != null && calcDao.isCalculationPublished(calculation.getId())) {
            throw new CalculationException(CalculationErrorCode.PERSIST_PUBLISHED);
        }
        calculationService.persistsInputFormValues(calculation, inputForm, inputFormValues);
    }

    /**
     * Setup.
     */
    @PostConstruct
    private void setup() {
        formulaVarPattern = Pattern.compile(formulaVarRegexp, Pattern.CASE_INSENSITIVE);
    }

    /**
     * Load formula data by hub id.
     * @param id hub id
     * @param ldts load datetime
     * @return formula instance
     */
    private Formula getFormulaById(Long id, LocalDateTime ldts) {
        String key = calcDao.getFormulaKeyById(id);
        return getFormulaByKey(key, ldts);
    }

    /**
     * Builds formula attributes.
     * @param pair pair, that contains formula to be calculated
     * @param evaluatedResults map of previously calculated results
     * @return map of formula attributes
     */
    private Map<String, Object> buildFormulaAttributes(Pair<String, Formula> pair, Map<Pair<String, Formula>, EvalResult> evaluatedResults) {
        Map<String, Object> attributes = new HashMap<>();
        pair.getRight().getChildren().stream()
            .filter(p -> !p.getRight().isLibrary())
            .forEach(p -> {
                EvalResult evalResult = evaluatedResults.get(p);
                attributes.put(p.getLeft(), evalResult != null ? evalResult.getResult() : null);
            });

        return attributes;
    }

    /**
     * Builds order of formula calculation.
     * @param rootFormula root formula
     * @return list of formulas in order of calculation
     */
    private List<Pair<String, Formula>> buildCalculationOrder(Formula rootFormula) {
        FormulaVisitor visitor = new FormulaVisitor();
        rootFormula.accept(null, visitor);
        return visitor.getCalcFormulas();
    }

    /**
     * Read calculation model objects from database.
     * @param entityMeta entity metadata
     * @param modelList list of parent model objects
     * @param useModelLdts use models ldts separately to load referenced objects
     * @param ldts load datetime
     */
    private void readModelReferences(EntityMeta entityMeta, List<Model> modelList, boolean useModelLdts, LocalDateTime ldts) {
        entityDbService.readEntityAttributes(modelList, ldts, useModelLdts, null);
        final Map<Long, List<Model>> modelMap = modelList.stream().collect(Collectors.groupingBy(Model::getHubId, Collectors.toList()));

        readModelReferredEntityMeta(modelList, modelMap, ldts, ModelAttributeMeta.INPUT_FORMS, EntityType.INPUT_FORM);
        readModelReferredEntityMeta(modelList, modelMap, ldts, ModelAttributeMeta.CLASSIFIERS, EntityType.CLASSIFIER);
        readModelReferredFormTemplate(modelList, modelMap, ldts);

        Criteria criteria = new Criteria();
        if (useModelLdts) {
            criteria.setHubIdsAndLdts(new HashSet<>());
        } else {
            criteria.setHubIds(new HashSet<>());
        }
        // EntityAttribute[childHubId], one child entity may be referenced by few parent attribute (many2many, many2one)
        Map<Long, List<EntityAttribute<Formula>>> childIdToParentForFormula =
                modelList.stream()
                         .filter((e) -> e.isAttributeExists(ModelAttributeMeta.FORMULAS.getKey()))
                         .flatMap((e) -> ((LinkedEntityAttribute<Formula>) e.getAttribute(ModelAttributeMeta.FORMULAS.getKey()))
                                 .getEntityAttributeList().stream())
                         .peek(e -> {
                             if (useModelLdts) {
                                 Collection<Pair<Long, LocalDateTime>> mIdLdts =
                                         modelMap.get(e.getMainHubId()).stream()
                                                 .map((Model m) -> Pair.of(e.getLinkedHubId(), m.getActuality()))
                                                 .collect(Collectors.toSet());
                                 criteria.getHubIdsAndLdts().addAll(mIdLdts);
                             } else {
                                 criteria.getHubIds().add(e.getLinkedHubId());
                             }
                         })
                         .collect(Collectors.groupingBy((EntityAttribute<Formula> ea) -> ea.getLinkedHubId(), Collectors.toList()));
        if (!childIdToParentForFormula.isEmpty()) {
            List<Formula> formulas = getFormulas(criteria, ldts);
            for (Formula formula : formulas) {
                List<EntityAttribute<Formula>> entityAttributesList = childIdToParentForFormula.get(formula.getHubId());
                if (entityAttributesList != null) {
                    for (EntityAttribute ea : entityAttributesList) {
                        ea.setEntity(formula);
                    }
                }
            }
        }
    }

    /**
     * Read modeless data from attribute referred to entity metadata.
     * @param modelList list of models
     * @param modelMap map of same models as at list
     * @param ldts load datetime
     * @param refAttributeE referenced attribute enumeration "key"
     * @param entityType kind of metadata type
     */
    private void readModelReferredEntityMeta(List<Model> modelList, final Map<Long, List<Model>> modelMap, LocalDateTime ldts,
                                             ModelAttributeMeta refAttributeE, EntityType entityType) {
        Criteria criteria = new Criteria();
        criteria.setHubIdsAndLdts(new HashSet<>());

        EntityMeta entityMetadata = entityMetaService.getEntityMetaByKey(EntityMeta.METADATA_KEY, ldts);
        AttributeMeta viewOrderMeta = entityMetadata.getAttributeMetadata(EntityMeta.EntityMetaAttributeMeta.VIEW_ORDER.getKey());
        criteria.getOrder().addItem(viewOrderMeta, false);

        // EntityAttribute[childHubId], one child entity may be referenced by few parent attribute (many2many, many2one)
        Map<Long, List<EntityAttribute<EntityMeta>>> childIdToParentForEntityMeta =
                (Map) modelList.stream()
                               .filter((e) -> e.isAttributeExists(refAttributeE.getKey()))
                               .flatMap((e) -> ((LinkedEntityAttribute<EntityMeta>) e.getAttribute(refAttributeE.getKey())).getEntityAttributeList()
                                                                                                                           .stream())
                               .peek(e -> {
                                   Collection<Pair<Long, LocalDateTime>> mIdLdts = modelMap.get(e.getMainHubId()).stream()
                                                                                           .map((Model m) -> Pair
                                                                                                   .of(e.getLinkedHubId(), m.getActuality()))
                                                                                           .collect(Collectors.toSet());
                                   criteria.getHubIdsAndLdts().addAll(mIdLdts);
                               })
                               .collect(Collectors.groupingBy((EntityAttribute<EntityMeta> ea) -> ea.getLinkedHubId(), Collectors.toList()));

        if (!childIdToParentForEntityMeta.isEmpty()) {
            List<EntityMeta> entityMetas = entityMetaService.getEntityMetas(criteria, null, ldts, entityType);
            for (EntityMeta entityMeta : entityMetas) {
                List<EntityAttribute<EntityMeta>> entityAttributesList = childIdToParentForEntityMeta.get(entityMeta.getHubId());
                if (entityAttributesList != null) {
                    for (EntityAttribute ea : entityAttributesList) {
                        ea.setEntity(entityMeta);
                    }
                }
            }
        }
    }

    /**
     * Read references to form template.
     * @param modelList models
     * @param modelMap map of same models as at list
     * @param ldts load datetime
     */
    private void readModelReferredFormTemplate(List<Model> modelList, final Map<Long, List<Model>> modelMap, LocalDateTime ldts) {
        Criteria criteria = new Criteria();
        criteria.setHubIdsAndLdts(new HashSet<>());

        // TODO need to extract common logic to methods from readModelReferredFormTemplate, readModelReferredEntityMeta, readModelReferences
        // EntityAttribute[childHubId], one child entity may be referenced by few parent attribute (many2many, many2one)
        Map<Long, List<EntityAttribute<FormTemplate>>> childIdToParentForFormTemplate = (Map)
                modelList.stream()
                         .filter((e) -> e.isAttributeExists(ModelAttributeMeta.FORM_TEMPLATES.getKey()))
                         .flatMap((e) -> ((LinkedEntityAttribute<FormTemplate>) e.getAttribute(ModelAttributeMeta.FORM_TEMPLATES.getKey()))
                                 .getEntityAttributeList().stream())
                         .peek((EntityAttribute<FormTemplate> ea) -> {
                             Collection<Pair<Long, LocalDateTime>> mIdLdts = modelMap.get(ea.getMainHubId()).stream()
                                                                                     .map((Model m) -> Pair.of(ea.getLinkedHubId(), m.getActuality()))
                                                                                     .collect(Collectors.toSet());
                             criteria.getHubIdsAndLdts().addAll(mIdLdts);
                         })
                         .collect(Collectors.groupingBy((EntityAttribute<FormTemplate> ea) -> ea.getLinkedHubId(), Collectors.toList()));

        if (!childIdToParentForFormTemplate.isEmpty()) {
            List<FormTemplate> formTemplates = formTemplateService.getFormTemplates(criteria, null, ldts);
            for (FormTemplate formTemplate : formTemplates) {
                List<EntityAttribute<FormTemplate>> entityAttributesList = childIdToParentForFormTemplate.get(formTemplate.getHubId());
                if (entityAttributesList != null) {
                    for (EntityAttribute ea : entityAttributesList) {
                        ea.setEntity(formTemplate);
                    }
                }
            }
        }
    }

    /**
     * Read information for calculations reference links.
     * @param entityMeta calculation metadata
     * @param calculationList list of calculations
     * @param ldts load datetime
     */
    private void readCalculationReferences(EntityMeta entityMeta, List<Calculation> calculationList, LocalDateTime ldts) {
        entityDbService.readEntityAttributes(calculationList, ldts, false, null);

        readCalculationReferencesUsers(calculationList, ldts);
        readCalculationReferencesModel(calculationList, ldts);
        readCalculationReferencesClient(calculationList, ldts);
        readCalculationReferencesClientGroup(calculationList, ldts);
        readCalculationReferencesProfile(calculationList, ldts);
        readCalculationReferencesFormulaResult(calculationList, ldts);
    }

    /**
     * Read calculation's users.
     * @param calculationList calculation list
     * @param ldts load datetime
     */
    private void readCalculationReferencesUsers(List<Calculation> calculationList, LocalDateTime ldts) {
        AttributeMeta userReferenceMeta = calculationList.get(0).getMeta().getAttributeMetadata(CalculationAttributeMeta.AUTHOR.getKey());
        readCalculationReferencesForAttribute(userReferenceMeta, calculationList, ldts);
    }

    /**
     * Load calculation clients.
     * @param calculationList calculation list
     * @param ldts load datetime
     */
    private void readCalculationReferencesClient(List<Calculation> calculationList, LocalDateTime ldts) {
        AttributeMeta clientReferenceMeta = calculationList.get(0).getMeta().getAttributeMetadata(CalculationAttributeMeta.CLIENT.getKey());
        readCalculationReferencesForAttribute(clientReferenceMeta, calculationList, ldts);
    }

    /**
     * Load calculation client groups.
     * @param calculationList calculation list
     * @param ldts load datetime
     */
    private void readCalculationReferencesClientGroup(List<Calculation> calculationList, LocalDateTime ldts) {
        AttributeMeta clientGroupReferenceMeta = calculationList.get(0).getMeta()
                                                                .getAttributeMetadata(CalculationAttributeMeta.CLIENT_GROUP.getKey());
        readCalculationReferencesForAttribute(clientGroupReferenceMeta, calculationList, ldts);
    }

    /**
     * Load calculation profiles.
     * @param calculationList calculation list
     * @param ldts load datetime
     */
    private void readCalculationReferencesProfile(List<Calculation> calculationList, LocalDateTime ldts) {
        AttributeMeta clientGroupReferenceMeta = calculationList.get(0).getMeta()
                                                                .getAttributeMetadata(CalculationAttributeMeta.CALC_PROFILE.getKey());
        readCalculationReferencesForAttribute(clientGroupReferenceMeta, calculationList, ldts);
    }

    /**
     * Load calculation referenced entity objects.
     * @param referenceAttributeMeta reference attribute metadata
     * @param calculationList list of calculation
     * @param ldts load datetime
     */
    private void readCalculationReferencesForAttribute(AttributeMeta referenceAttributeMeta, List<Calculation> calculationList, LocalDateTime ldts) {
        EntityMeta userMeta = entityMetaService.getEntityMetaByKey(referenceAttributeMeta.getEntityKey(), ldts);

        Criteria criteria = new Criteria();
        criteria.setHubIds(new ArrayList<>());
        // EntityAttribute[childHubId], one child entity may be referenced by few parent attribute (many2many, many2one)
        Map<Long, List<EntityAttribute<EntityMeta>>> childIdToParentForUsers =
                calculationList.stream()
                               .filter((e) -> e.isAttributeExists(referenceAttributeMeta.getKey()))
                               .flatMap((e) -> ((LinkedEntityAttribute<EntityMeta>) e.getAttribute(referenceAttributeMeta.getKey()))
                                       .getEntityAttributeList().stream())
                               .peek(e -> criteria.getHubIds().add(e.getLinkedHubId()))
                               .collect(Collectors.groupingBy((EntityAttribute<EntityMeta> ea) -> ea.getLinkedHubId(), Collectors.toList()));

        if (!childIdToParentForUsers.isEmpty()) {
            List<Entity> entities = (List<Entity>) entityService.getEntities(userMeta, criteria, null, ldts);
            for (Entity entity : entities) {
                List<EntityAttribute<EntityMeta>> entityAttributesList = childIdToParentForUsers.get(entity.getHubId());
                if (entityAttributesList != null) {
                    for (EntityAttribute ea : entityAttributesList) {
                        ea.setEntity(entity);
                    }
                }
            }
        }
    }

    /**
     * Read calculation's models.
     * @param calculationList calculation's list
     * @param ldts load datetime
     */
    private void readCalculationReferencesModel(List<Calculation> calculationList, LocalDateTime ldts) {
        EntityMeta modelEntityMetadata = getModelEntityMetadata(ldts);
        AttributeMeta hubIdAttributeMeta = modelEntityMetadata.getHubIdAttribute();
        AttributeMeta versionAttributeMeta = modelEntityMetadata.getAttributeMetadata(ModelAttributeMeta.VERSION.getKey());

        Criteria criteria = new Criteria();
        // get latest appropriate model
        criteria.setStrictLatestActualRecord(false);
        Where where = criteria.getWhere();
        // EntityAttribute[childHubId], one child entity may be referenced by few parent attribute (many2many, many2one)
        Map<Long, List<EntityAttribute<Model>>> childIdToParentForModels =
                calculationList.stream()
                               .filter((e) -> e.isAttributeExists(CalculationAttributeMeta.MODEL.getKey()))
                               .flatMap((e) -> ((LinkedEntityAttribute<Model>) e.getAttribute(CalculationAttributeMeta.MODEL.getKey()))
                                       .getEntityAttributeList().stream())
                               .peek(e -> {
                                   WhereParenthesesGroup wg =
                                           (where.isDefined()) ? new WhereParenthesesGroup(Conjunction.OR) : new WhereParenthesesGroup();
                                   wg.addItem(new WhereItem(hubIdAttributeMeta, Operator.EQ, e.getLinkedHubId()));
                                   if (e.isSatelliteDefined()) {
                                       BigDecimal bigDecimalVersion = (BigDecimal) e.getSatellite().getAttributeValue(
                                               CalculationModelAttributeMeta.VERSION.getKey());
                                       if (bigDecimalVersion != null) {
                                           wg.addItem(new WhereItem(versionAttributeMeta, Operator.EQ, bigDecimalVersion.longValue()));
                                           criteria.addPartitionAttribute(
                                                   modelEntityMetadata.getAttributeMetadata(ModelAttributeMeta.VERSION.getKey()));
                                       }
                                   }
                                   where.addGroup(wg);
                               })
                               .collect(Collectors.groupingBy((EntityAttribute<Model> ea) -> ea.getLinkedHubId(), Collectors.toList()));

        if (!childIdToParentForModels.isEmpty()) {
            List<Model> models = getModels(criteria, null, ldts, true);
            for (Model model : models) {
                List<EntityAttribute<Model>> entityAttributesList = childIdToParentForModels.get(model.getHubId());
                if (entityAttributesList != null) {
                    for (EntityAttribute ea : entityAttributesList) {
                        //if VESION defined and VERSION == VERSION
                        if (ea.isSatelliteDefined()) {
                            BigDecimal bigDecimalVersion = (BigDecimal) ea.getSatellite().getAttributeValue(
                                    CalculationModelAttributeMeta.VERSION.getKey());
                            if (bigDecimalVersion != null && model.getVersion() != null) {
                                if (bigDecimalVersion.longValue() == model.getVersion().longValue())
                                    ea.setEntity(model);
                            } else {
                                ea.setEntity(model);
                            }
                        } else {
                            ea.setEntity(model);
                        }
                    }
                }
            }
        }
    }

    /**
     * Read calculation's formula results.
     * @param calculationList calculations list
     * @param ldts load datetime
     */
    private void readCalculationReferencesFormulaResult(List<Calculation> calculationList, LocalDateTime ldts) {
        // FormulaResult reading
        List<FormulaResultParameter> calculationFormulaResultParams = new ArrayList<>(); // to pass to query
        Map<Triple<Long, Long, Long>, Triple<Calculation, Entity, Formula>> formulaBackIndex = new HashMap<>(); // to distribute query result,
        // one formula id may be instantiated for deferment calculations
        for (Calculation calculation : calculationList) {
            if (calculation.getModel() != null) {
                for (Entity profile : calculation.getProfiles()) {
                    for (Formula formula : calculation.getModel().getFormulas()) {
                        formulaBackIndex.put(Triple.of(calculation.getHubId(), profile.getHubId(), formula.getHubId()),
                                             Triple.of(calculation, profile, formula));
                        calculationFormulaResultParams.add(FormulaResultParameter.of(calculation, profile, formula));
                    }
                }
            }
        }

        if (calculationFormulaResultParams.size() > 0) {
            Map<Long, FormulaResultMultiLinkDto> linkDtoMap = calcDao.readLinkCalcFormulaResult(calculationFormulaResultParams, ldts);
            if (linkDtoMap.size() > 0) {
                Criteria criteria = new Criteria();
                criteria.setHubIds(linkDtoMap.keySet());
                List<FormulaResult> formulaResultList = this.getFormulaResults(criteria, null, ldts);

                // distribute formula result by formulas
                for (FormulaResult formulaResult : formulaResultList) {
                    for (FormulaResultMultiLinkDtoReference lr : linkDtoMap.get(formulaResult.getHubId()).getReference()) {
                        Triple<Calculation, Entity, Formula> calcAndFormula = formulaBackIndex
                                .get(Triple.of(lr.getCalculationId(), lr.getCalcProfileId(), lr.getFormulaId()));
                        Formula formula = calcAndFormula.getRight();
                        Calculation calculation = calcAndFormula.getLeft();
                        Entity profile = calcAndFormula.getMiddle();
                        if (formula != null)
                            formula.setFormulaResult(formulaResult, calculation, profile);
                    }
                }
            }
        }
    }

    /**
     * Enriches criteria. It doesn't modify origin criteria
     * @param criteria filter criteria
     * @param strictLatestActualRecord strict latest actual record flag
     * @param whereItems criteria conditions
     * @return enriched criteria object
     */
    private Criteria getEnrichedCriteria(final Criteria criteria, boolean strictLatestActualRecord, WhereItem... whereItems) {
        Criteria enrichedCriteria = (criteria == null) ? new Criteria() : ObjectUtils.clone(criteria);
        enrichedCriteria.setStrictLatestActualRecord(strictLatestActualRecord);
        Stream.of(whereItems).forEach(wi -> enrichedCriteria.getWhere().addItem(wi));
        return enrichedCriteria;
    }

    /**
     * Returns calculation model metadata.
     * @param ldts load datetime
     * @return entity metadata
     */
    private EntityMeta getModelEntityMetadata(LocalDateTime ldts) {
        return entityMetaService.getEntityMetaByKey(Model.METADATA_KEY, ldts);
    }

    /**
     * Return calculation metadata.
     * @param ldts load datetime
     * @return entity metadata
     */
    private EntityMeta getCalculationEntityMetadata(LocalDateTime ldts) {
        return entityMetaService.getEntityMetaByKey(Calculation.METADATA_KEY, ldts);
    }

    /**
     * Returns common formula result map.
     * If someone needs to suppress this type of optimization he must return empty map from this method.
     * @param formulas list of common formula trees
     * @param calculation calculation object
     * @param profile calculation profile
     * @return common formula result map
     */
    private Map<String, EvalResult> getCommonFormulaResults(List<Formula> formulas, Calculation calculation, Entity profile) {
        Map<String, EvalResult> commonResults = new HashMap<>();
        Map<Pair<String, Formula>, EvalResult> evaluatedResults = new HashMap<>();
        getCommonFormulas(formulas).stream().parallel().forEach(formula -> {
            for (Pair<String, Formula> pair : buildCalculationOrder(formula)) {
                EvalResult result = eval(pair.getRight(),
                                         formulaService.buildBindings(buildFormulaAttributes(pair, evaluatedResults), calculation, profile));
                if (LOG.isDebugEnabled()) {
                    LOG.debug("\n#getCommonFormulaResults (" + profile.getKey() + ")--------------------------------------\n"
                              + "Formula key: " + pair.getRight().getKey() + "\n"
                              + "Formula data: \n" + pair.getRight().getFormula().getData() + "\n"
                              + "Result: '" + result.getResult() + "'\n"
                              + "Output: \n" + result.getOutput().trim() + "\n"
                              + "Exception: \n" + result.getException() + "\n"
                              + "--------------------------------------------------------------------------\n");
                }

                if (!pair.getRight().isLibrary()) {
                    saveEvaluationResults(calculation, profile, pair.getRight(), result);
                }
                if (result.getException() != null) {
                    LOG.warn("Error evaluating formula.", result.getException());
                    break;
                }
                evaluatedResults.put(pair, result);
                commonResults.put(pair.getRight().getKey(), result);
            }
        });
        return commonResults;
    }

    /**
     * Returns list of local-root formulas, that are common between different formula trees.
     * @param formulas list of formulas with their children set
     * @return list of common formulas
     */
    private List<Formula> getCommonFormulas(List<Formula> formulas) {
        Map<Formula, Integer> enconters = new HashMap<>();
        formulas.forEach(f -> {
            putChildFormulasToMap(enconters, f);
            putFormulaToMap(enconters, f);
        });
        List<Formula> commonFormulas = enconters.keySet().stream().filter(f -> (enconters.get(f) > 1)).collect(Collectors.toList());
        return commonFormulas.stream().filter(f -> !isChildOrLibrary(commonFormulas, f)).collect(Collectors.toList());
    }

    /**
     * Flattens formula trees to map of formula as key and amount of it encounters among different formula trees.
     * @param enconters map of encounters
     * @param formula formula to put
     */
    private void putChildFormulasToMap(Map<Formula, Integer> enconters, Formula formula) {
        formula.getChildren().forEach(p -> {
            putChildFormulasToMap(enconters, p.getRight());
            putFormulaToMap(enconters, p.getRight());
        });
    }

    /**
     * Puts formula to map, and increments encounter number.
     * @param enconters map
     * @param f {@link Formula} instance
     */
    private void putFormulaToMap(Map<Formula, Integer> enconters, Formula f) {
        Integer cnt = enconters.get(f);
        if (cnt == null) {
            cnt = 0;
        }
        enconters.put(f, ++cnt);
    }

    /**
     * Returns true, if passed formula is library one, or exist as child one in list of common among different formula trees.
     * @param formulas list of formulas, common among different formula trees
     * @param testFormula formula to test
     * @return boolean
     */
    private boolean isChildOrLibrary(List<Formula> formulas, Formula testFormula) {
        if (testFormula.isLibrary()) {
            return true;
        }
        for (Formula f : formulas) {
            if (f.equals(testFormula)) {
                continue;
            }
            if (isChild(f.getChildren(), testFormula)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true, if passed <em>testFormula</em> exists as child one in passed list of <em>children</em> or their sub-children
     * (uses recursion to decide, whether this is true), false otherwise.
     * @param children list of {@link FormulaDependencyPair} instance
     * @param testFormula formula to test
     * @return boolean
     */
    private boolean isChild(List<FormulaDependencyPair> children, Formula testFormula) {
        boolean child = false;
        for (FormulaDependencyPair pair : children) {
            if (pair.getRight().equals(testFormula)) {
                return true;
            }
            child = !child && isChild(pair.getRight().getChildren(), testFormula);
        }
        return child;
    }

    /**
     * Check sets changed.
     * @param source source
     * @param target target
     * @return flag sets changed
     */
    private boolean isSetsChanged(List<?> source, List<?> target) {
        boolean isChanged = false;

        if (source != null) {
            if (target != null) {
                if (source.size() != 0) {
                    if (target.size() != 0) {
                        isChanged = CollectionUtils.disjunction(source, target).size() != 0;
                    } else isChanged = true;
                } else {
                    if (target.size() != 0) isChanged = true;
                }
            } else isChanged = true;
        } else if (target != null) isChanged = true;

        return isChanged;
    }

    /**
     * Check form's sattelite attributes changed.
     * @param storedModel stored model
     * @param model model
     * @return flag sets changed
     */
    private boolean isFormPeriodsChanged(Model storedModel, Model model) {
        List<EntityAttribute<EntityMeta>> storedAttributes = ((LinkedEntityAttribute<EntityMeta>) storedModel
                .getAttribute(Model.ModelAttributeMeta.INPUT_FORMS.getKey())).getEntityAttributeList();
        List<EntityAttribute<EntityMeta>> attributes = ((LinkedEntityAttribute<EntityMeta>) model
                .getAttribute(Model.ModelAttributeMeta.INPUT_FORMS.getKey())).getEntityAttributeList();
        for (EntityAttribute<EntityMeta> attribute : attributes) {
            EntityAttribute<EntityMeta> storedAttribute =
                    storedAttributes.stream().filter(ea -> ea.getEntity().getKey().equals(attribute.getEntity().getKey())).findFirst().orElse(null);
            if (storedAttribute == null)
                return true;
            Object dateAttr = attribute.getSatellite().getAttributeValue(Model.ModelInputFormAttributeMeta.INPUT_FORM_DATE_ATTR_KEY.getKey());
            Object storedDateAttr = storedAttribute.getSatellite()
                                                   .getAttributeValue(Model.ModelInputFormAttributeMeta.INPUT_FORM_DATE_ATTR_KEY.getKey());
            Object periods = attribute.getSatellite().getAttributeValue(Model.ModelInputFormAttributeMeta.PERIOD_COUNT.getKey());
            Object storedPeriods = storedAttribute.getSatellite().getAttributeValue(Model.ModelInputFormAttributeMeta.PERIOD_COUNT.getKey());
            if (ObjectUtils.notEqual(dateAttr, storedDateAttr) || ObjectUtils.notEqual(periods, storedPeriods))
                return true;
        }
        return false;
    }
}
