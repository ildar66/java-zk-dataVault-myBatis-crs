package ru.masterdm.crs.service.calc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.masterdm.crs.dao.calc.CalcDao;
import ru.masterdm.crs.dao.calc.dto.FormulaResultMultiLinkDto;
import ru.masterdm.crs.dao.calc.dto.FormulaResultMultiLinkDtoReference;
import ru.masterdm.crs.dao.calc.dto.FormulaResultParameter;
import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.Formula;
import ru.masterdm.crs.domain.calc.FormulaDependencyPair;
import ru.masterdm.crs.domain.calc.FormulaResult;
import ru.masterdm.crs.domain.calc.FormulaVisitor;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.calc.formula.SimpleDataAccessService;
import ru.masterdm.crs.util.annotation.CurrentTimeStamp;

/**
 * {@link FormulaService} implementation.
 * @author Alexey Chalov
 */
@Service
public class FormulaServiceImpl implements FormulaService {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private CalcDao calcDao;
    @Autowired
    private EntityService entityService;
    @Autowired
    private EntityMetaService entityMetaService;

    private ScriptEngineManager engineManager;
    @Autowired
    private SimpleDataAccessService simpleDataAccessService;

    @Override
    public Map<String, Object> buildBindings(Map<String, Object> map, Calculation calculation, Entity profile) {
        if (map == null)
            map = new HashMap<>();

        // TODO create plug-based mechanics to fill bindings
        map.put("currentCalculation", calculation);
        map.put("currentProfile", profile);
        map.put("simpleDataAccessService", simpleDataAccessService);
        return map;
    }

    @Async
    @Override
    public Future<Object> eval(Formula formula, ScriptContext ctx) throws ScriptException {
        ScriptEngine engine = engineManager.getEngineByName(formula.getEvalLang().toLowerCase());
        for (Pair<String, Formula> p : formula.getChildren()) {
            if (p.getRight().isLibrary()) {
                FormulaVisitor visitor = new FormulaVisitor();
                p.getRight().accept(null, visitor);
                for (Pair<String, Formula> pair : visitor.getCalcFormulas()) {
                    if (pair.getRight().isLibrary()) {
                        engine.eval(pair.getRight().getFormula().getData(), ctx);
                    }
                }
            }
        }
        return new AsyncResult<>(engine.eval(formula.getFormula().getData(), ctx));
    }

    @Override
    public Formula getFormulaTree(Formula root, LocalDateTime actualityDate) {
        List<Formula> list = calcDao.getFormulaFlattenedTree(root, actualityDate);
        Map<String, Formula> formulaMap = new HashMap<>();
        list.forEach(p -> formulaMap.put(p.getKey(), p));

        root = formulaMap.get(root.getKey());
        setupChildren(root, formulaMap);
        return root;
    }

    @Transactional
    @Override
    public List<Formula> getFormulaTrees(Criteria criteria, Calculation calculation, LocalDateTime ldts) {
        LocalDateTime formulaLoadDate = (calculation != null) ? calculation.getModel().getActuality() : ldts;
        calcDao.prepareFilteredFlattenedFormulaTrees(criteria, calculation, formulaLoadDate);
        List<Formula> formulas = calcDao.getFilteredFlattenedFormulaTrees(criteria);

        List<Formula> roots = new ArrayList<>();
        formulas.forEach(f -> {
            boolean root = true;
            root_search_loop:
            for (Formula f1 : formulas) {
                for (Pair<String, Formula> pair : f1.getChildren()) {
                    if (f.getHubId().equals(pair.getRight().getHubId())) {
                        root = false;
                        break root_search_loop;
                    }
                }
            }
            if (root) {
                roots.add(f);
            }
        });
        roots.forEach(r -> setupChildren(r, formulas));

        if (calculation != null && !roots.isEmpty()) {
            loadFormulaResult(calculation, roots, ldts);
        }

        return roots;
    }

    @Override
    public List<FormulaResult> getFormulaResults(Criteria criteria, RowRange rowRange, @CurrentTimeStamp LocalDateTime ldts) {
        EntityMeta entityMeta = getFormulaResultEntityMetadata(ldts);
        return (List<FormulaResult>) entityService.getEntitiesBase(entityMeta, criteria, rowRange, ldts);
    }

    /**
     * Return formula result metadata.
     * @param ldts load datetime
     * @return entity metadata
     */
    private EntityMeta getFormulaResultEntityMetadata(LocalDateTime ldts) {
        return entityMetaService.getEntityMetaByKey(FormulaResult.METADATA_KEY, ldts);
    }

    /**
     * Load {@link FormulaResult formula results} into formulas.
     * @param calculation calculation for results
     * @param rootFormulas formulas
     * @param ldts load datetime
     */
    private void loadFormulaResult(Calculation calculation, Collection<Formula> rootFormulas, LocalDateTime ldts) {
        Set<FormulaResultParameter> calculationFormulaResultParams = new HashSet<>(); // to pass to query
        Map<Long, Formula> formulaBackIndex = new HashMap<>(); // to distribute query result,

        if (calculation.getModel() != null) {
            for (Formula formula : rootFormulas) {
                FormulaVisitor visitor = new FormulaVisitor();
                formula.accept(null, visitor);
                for (Pair<String, Formula> p : visitor.getCalcFormulas()) {
                    formulaBackIndex.put(p.getRight().getHubId(), p.getRight());
                    for (Entity profile : calculation.getProfiles())
                        calculationFormulaResultParams.add(FormulaResultParameter.of(calculation, profile, p.getRight()));
                }
            }
        }

        if (calculationFormulaResultParams.size() > 0) {
            Map<Long, FormulaResultMultiLinkDto> linkDtoMap = calcDao.readLinkCalcFormulaResult(calculationFormulaResultParams, ldts);
            if (linkDtoMap.size() > 0) {
                Map<Long, Entity> profilesMap = calculation.getProfiles().stream().collect(Collectors.toMap(Entity::getHubId, p -> p));
                Criteria criteria = new Criteria();
                criteria.setHubIds(linkDtoMap.keySet());
                List<FormulaResult> formulaResultList = this.getFormulaResults(criteria, null, ldts);

                // distribute formula result by formulas
                for (FormulaResult formulaResult : formulaResultList) {
                    for (FormulaResultMultiLinkDtoReference lr : linkDtoMap.get(formulaResult.getHubId()).getReference()) {
                        Formula formula = formulaBackIndex.get(lr.getFormulaId());
                        Entity profile = profilesMap.get(lr.getCalcProfileId());
                        if (formula != null)
                            formula.setFormulaResult(formulaResult, calculation, profile);
                    }
                }
            }
        }
    }

    /**
     * Performs bean post initialization.
     */
    @PostConstruct
    private void initialize() {
        engineManager = new ScriptEngineManager();
    }

    /**
     * Recursively setups formula children.
     * @param formula {@link Formula} instance
     * @param formulaMap {@link Map} instance
     */
    private void setupChildren(Formula formula, Map<String, Formula> formulaMap) {
        List<FormulaDependencyPair> newChildren = new ArrayList<>();
        formula.getChildren().forEach(p -> {
            Formula newFormula = formulaMap.get(p.getRight().getKey());
            newChildren.add(FormulaDependencyPair.of(p.getLeft(), newFormula));
            setupChildren(newFormula, formulaMap);
        });
        formula.setChildren(newChildren);
    }

    /**
     * Recursively setups formula children.
     * @param root root {@link Formula} instance
     * @param formulas list of formula flattened trees
     */
    private void setupChildren(Formula root, List<Formula> formulas) {
        List<FormulaDependencyPair> newChildren = new ArrayList<>();
        formulas.forEach(f -> root.getChildren().forEach(ch -> {
            if (ch.getRight().getHubId().equals(f.getHubId())) {
                newChildren.add(FormulaDependencyPair.of(null, f));
                setupChildren(f, formulas);
            }
        }));
        root.setChildren(newChildren);
    }
}
