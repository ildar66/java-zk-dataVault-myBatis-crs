package ru.masterdm.crs.web.model.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Default;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.SmartNotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModelList;

import ru.masterdm.crs.domain.calc.Formula;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.FormulaType;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.web.domain.ReferencedAttributeMetaPair;
import ru.masterdm.crs.web.domain.UserProfile;
import ru.masterdm.crs.web.domain.entity.EntityFilter;

/**
 * Edit entities filter view model class.
 * @author Igor Matushak
 * @author Alexey Kirilchev
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EditEntitiesFilterViewModel {

    public static final String CALC_FORMULA_MODEL = "CALC_FORMULA_MODEL";
    public static final String CALC_FORMULA_CALCULATION = "CALC_FORMULA_CALCULATION";

    @WireVariable("webConfig")
    private Properties webConfig;
    @WireVariable("userProfile")
    private UserProfile userProfile;
    @WireVariable
    private EntityService entityService;
    @WireVariable
    private EntityMetaService entityMetaService;

    private EntityMeta entityMeta;
    private ListModelList<EntityFilter> entityFilters;
    private List<ReferencedAttributeMetaPair> referencedAttributeMetaPairs;
    private Predicate<AttributeMeta> restrictedAttributes;
    private String filterKey;
    private String resetFilterName;

    /**
     * Initiates context.
     * @param entityMeta entity meta
     * @param restrictedAttributes restricted attributes
     * @param filterKey filter key
     * @param resetFilterName reset filter name
     */
    @Init
    public void initSetup(@ExecutionArgParam("entityMeta") EntityMeta entityMeta,
                          @ExecutionArgParam("restrictedAttributes") Predicate<AttributeMeta> restrictedAttributes,
                          @ExecutionArgParam("filterKey") String filterKey,
                          @ExecutionArgParam("resetFilterName") @Default("resetEntitiesFilter") String resetFilterName) {
        this.filterKey = filterKey != null ? filterKey : entityMeta.getKey();
        this.entityMeta = entityMeta;
        this.resetFilterName = resetFilterName;
        this.entityFilters = new ListModelList<>(userProfile.getFiltersByKey(this.filterKey));
        this.restrictedAttributes = restrictedAttributes == null ? attributeMeta -> true : restrictedAttributes;
    }

    /**
     * Detaches window.
     * @param view view
     */
    @Command
    public void detachWindow(@ContextParam(ContextType.VIEW) Component view) {
        view.detach();
        BindUtils.postGlobalCommand(null, null, "entitiesRefresh", null);
    }

    /**
     * Saves entity filter.
     * @param view view
     */
    @Command
    public void saveEntityFilter(@ContextParam(ContextType.VIEW) Component view) {
        view.detach();
        userProfile.setFiltersByKey(filterKey, entityFilters.getInnerList());
        BindUtils.postGlobalCommand(null, null, resetFilterName, null);
    }

    /**
     * Removes entity filter.
     * @param entityFilter entity filter
     */
    @Command
    @SmartNotifyChange("*")
    public void removeEntityFilter(@BindingParam("entityFilter") EntityFilter entityFilter) {
        entityFilters.remove(entityFilter);
    }

    /**
     * Adds entities filter.
     */
    @Command
    @SmartNotifyChange("*")
    public void addEntitiesFilter() {
        EntityFilter entityFilter = getNewEntityFilter();
        entityFilters.add(entityFilter);
    }

    /**
     * Changes operator.
     * @param entityFilter entity filter
     */
    @Command
    @SmartNotifyChange("*")
    public void changeOperator(@BindingParam("entityFilter") EntityFilter entityFilter) {
        if (entityFilter.getOperator().equals(Operator.IS_NULL) || entityFilter.getOperator().equals(Operator.IS_NOT_NULL)) {
            entityFilter.setValue(null);
        }
    }

    /**
     * Changes attribute meta.
     * @param entityFilter entity filter
     */
    @Command
    @SmartNotifyChange("*")
    public void changeAttributeMeta(@BindingParam("entityFilter") EntityFilter entityFilter) {
        entityFilter.setValue(AttributeType.BOOLEAN.equals(entityFilter.getAttributeMeta().getType()) ? false : null);
        if (entityFilter.getAttributeMeta().getType().equals(AttributeType.REFERENCE) && !entityFilter.getOperator().equals(Operator.IS_NULL)
            && !entityFilter.getOperator().equals(Operator.IS_NOT_NULL)) {
            entityFilter.setOperator(Operator.IS_NULL);
        }
    }

    /**
     * Returns entity filters.
     * @return entity filters
     */
    public List<EntityFilter> getEntityFilters() {
        return entityFilters;
    }

    /**
     * Returns new entity filter.
     * @return new entity filter
     */
    private EntityFilter getNewEntityFilter() {
        EntityFilter entityFilter = new EntityFilter();
        entityFilter.setOperator(Operator.EQ);
        entityFilter.setDateFormat(Labels.getLabel("date_format"));
        entityFilter.setDateTimeFormat(Labels.getLabel("date_time_format"));
        entityFilter.setReferencedAttributeMetaPair(ReferencedAttributeMetaPair.of(entityMeta.getKeyAttribute(), null));
        entityFilter.setLocale(userProfile.getLocale());
        return entityFilter;
    }

    /**
     * Returns referenced attribute meta pair list.
     * @return referenced attribute meta pair list
     */
    public List<ReferencedAttributeMetaPair> getReferencedAttributeMetaPairs() {
        if (referencedAttributeMetaPairs == null) {
            referencedAttributeMetaPairs = new ArrayList<>();

            List<AttributeMeta> attributeMetaList = new ArrayList<>();
            attributeMetaList.add(entityMeta.getKeyAttribute());
            attributeMetaList.addAll(entityMeta.getAttributes().stream()
                                               .filter(p -> p.isFilterAvailable() && !p.getType().equals(AttributeType.FILE))
                                               .filter(restrictedAttributes)
                                               .collect(Collectors.toList()));

            for (AttributeMeta attributeMeta : attributeMetaList) {
                if (attributeMeta.getType().equals(AttributeType.REFERENCE)) {
                    referencedAttributeMetaPairs.add(ReferencedAttributeMetaPair.of(attributeMeta, null));
                    EntityMeta referencedEntityMeta = entityMetaService.getEntityMetaByKey(attributeMeta.getEntityKey(), null);
                    for (AttributeMeta referencedAttributeMeta : referencedEntityMeta.getAttributes()) {
                        if (referencedAttributeMeta.isFilterAvailable() && !referencedAttributeMeta.getType().equals(AttributeType.FILE)
                            && !referencedAttributeMeta.getType().equals(AttributeType.REFERENCE)) {
                            referencedAttributeMetaPairs.add(ReferencedAttributeMetaPair.of(attributeMeta, referencedAttributeMeta));
                        }
                    }
                } else {
                    referencedAttributeMetaPairs.add(ReferencedAttributeMetaPair.of(attributeMeta, null));
                }
            }
        }
        return referencedAttributeMetaPairs;
    }

    /**
     * Returns operators.
     * @param entityFilter entity filter
     * @return operators
     */
    public List<Operator> getOperators(EntityFilter entityFilter) {
        if (entityFilter.getReferencedAttributeMetaPair().getReferencedAttributeMeta() != null) {
            AttributeMeta attributeMeta = entityFilter.getReferencedAttributeMetaPair().getReferencedAttributeMeta();
            return Arrays.stream(Operator.values()).filter(p -> !p.equals(Operator.IN)
                                                                && !(Formula.FormulaAttributeMeta.TYPE.getKey().equals(attributeMeta.getKey())
                                                                     && !(p == Operator.EQ || p == Operator.NOT_IN))
                                                                && !(AttributeType.BOOLEAN.equals(attributeMeta.getType())
                                                                     && !(p == Operator.IS_NULL || p == Operator.IS_NOT_NULL || p == Operator.EQ))
            ).collect(Collectors.toList());
        } else {
            AttributeMeta attributeMeta = entityFilter.getReferencedAttributeMetaPair().getAttributeMeta();

            return Arrays.stream(Operator.values()).filter(p -> !p.equals(Operator.IN)
                                                                && !(Formula.FormulaAttributeMeta.TYPE.getKey().equals(attributeMeta.getKey())
                                                                     && !(p == Operator.EQ || p == Operator.NOT_IN))
                                                                && !(AttributeType.REFERENCE.equals(attributeMeta.getType())
                                                                     && !(p == Operator.IS_NULL || p == Operator.IS_NOT_NULL))
                                                                && !(AttributeType.BOOLEAN.equals(attributeMeta.getType())
                                                                     && !(p == Operator.IS_NULL || p == Operator.IS_NOT_NULL || p == Operator.EQ))
            ).collect(Collectors.toList());
        }
    }

    /**
     * Returns formula types map.
     * @return formula types map
     */
    public Map<FormulaType, String> getFormulaTypes() {
        return Arrays.stream(FormulaType.values())
                     .filter(v -> !(CALC_FORMULA_MODEL.equals(filterKey) && (v == FormulaType.LIBRARY
                                                                             || v == FormulaType.SYS_LIBRARY
                                                                             || v == FormulaType.PRECALCULATED_FORMULA))
                                  && !(CALC_FORMULA_CALCULATION.equals(filterKey) && (v == FormulaType.LIBRARY
                                                                                      || v == FormulaType.SYS_LIBRARY))
                     )

                     .collect(Collectors.toMap(v -> v, v -> Labels.getLabel(v.name()),
                                               (u, v) -> {
                                                   return u;
                                               },
                                               LinkedHashMap::new));
    }

    /**
     * Returns calculation formula type key.
     * @return calculation formula type key
     */
    public String getCalcFormulaTypeKey() {
        return Formula.FormulaAttributeMeta.TYPE.getKey();
    }

    /**
     * Returns reference attribute meta pair label.
     * @param referencedAttributeMetaPair reference attribute meta pair.
     * @return reference attribute meta pair label
     */
    public String getReferencedAttributeMetaPairLabel(
            @BindingParam("referencedAttributeMetaPair") ReferencedAttributeMetaPair referencedAttributeMetaPair) {
        if (referencedAttributeMetaPair.getReferencedAttributeMeta() == null) {
            return String.format("%s", referencedAttributeMetaPair.getAttributeMeta().getName().getDescription(userProfile.getLocale()));
        } else {
            return String.format("%s (%s)", referencedAttributeMetaPair.getAttributeMeta().getName().getDescription(userProfile.getLocale()),
                                 referencedAttributeMetaPair.getReferencedAttributeMeta().getName().getDescription(userProfile.getLocale()));
        }
    }
}
