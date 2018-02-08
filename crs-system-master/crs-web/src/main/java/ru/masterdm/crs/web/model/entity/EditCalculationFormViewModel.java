package ru.masterdm.crs.web.model.entity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zul.ListModelList;

import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.meta.AttributeLocale;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.web.domain.entity.EntityStatus;

/**
 * Edit calculation form view model class.
 * @author Alexey Kirilchev
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EditCalculationFormViewModel extends EditEntitiesViewModel {

    private List<EntityMeta> inputForms;
    private String inputFormName;
    private Calculation calculation;

    /**
     * Initiates context.
     * @param calculation calculation
     */
    @Init
    public void initSetup(@ExecutionParam("calculation") Calculation calculation) {
        pageSize = Integer.parseInt(webConfig.getProperty("pageSize"));
        this.calculation = calculation;
    }

    /**
     * Sets current calculation.
     * @param calculation calculation
     */
    @GlobalCommand
    public void setCurrentCalculation(@BindingParam("calculation") Calculation calculation) {
        this.calculation = calculation;
        BindUtils.postNotifyChange(null, null, this, "*");
    }

    /**
     * Returns input form list.
     * @return input form list
     */
    public List<EntityMeta> getInputForms() {
        if (inputForms == null && calculation != null && calculation.getModel() != null) {
            List<EntityMeta> inputFormList = calculation.getModel().getInputForms();
            if (inputFormList != null) {
                String searchStr = inputFormName != null ? inputFormName.trim().toLowerCase() : "";
                AttributeLocale locale = userProfile.getLocale();
                inputForms = inputFormList.stream()
                                          .filter(e -> e.getName() != null && e.getName().getDescription(locale).toLowerCase().contains(searchStr))
                                          .sorted(Comparator.comparing(e -> e.getName().getDescription(locale).toLowerCase()))
                                          .collect(Collectors.toList());
            }
        }
        return inputForms;
    }

    /**
     * Selects entity meta.
     */
    @Command
    public void selectInputForm() {
        entityMetaKey = entityMeta.getKey();
        entityMeta = entityMetaService.getEntityMetaByKey(entityMetaKey, getActuality());
        entityFilters = new ListModelList<>(userProfile.getFiltersByKey(entityMetaKey));
        entityFilters.setMultiple(true);
        userProfile.getFiltersByKey(entityMetaKey).stream().forEach(ef -> entityFilters.addToSelection(ef));
        updateCriteria();
        entityStatuses = null;
        BindUtils.postNotifyChange(null, null, this, "*");
    }

    /**
     * Reload data on input form name changed.
     */
    @Command
    public void inputFormNameChanged() {
        inputForms = null;
        BindUtils.postNotifyChange(null, null, this, "inputForms");
    }

    /**
     * Returns input form name.
     * @return input form name
     */
    public String getInputFormName() {
        return inputFormName;
    }

    /**
     * Sets input form name.
     * @param inputFormName input form name
     */
    public void setInputFormName(String inputFormName) {
        this.inputFormName = inputFormName;
    }

    @Override
    protected List<Entity> getEntities() {
        RowRange rowRange = RowRange.newAsPageAndSize(activePage, pageSize);
        List<Entity> entities = calcService.getInputFormValues(calculation, entityMeta, criteria, rowRange, null);
        totalSize = rowRange.getTotalCount();
        return entities;
    }

    @Override
    @Command
    public void addEntity() {
        Entity entity = entityService.newEmptyEntity(getEntityMeta());
        entityStatuses.add(0, new EntityStatus(entity, true));
        BindUtils.postNotifyChange(null, null, this, "entityStatuses");
    }

    @Override
    public List<AttributeMeta> getAttributes() {
        return getEntityMeta() != null ? getEntityMeta().getAttributes().stream()
                                                        .filter(a -> !(a.getType() == AttributeType.REFERENCE
                                                                       && Calculation.METADATA_KEY.equals(a.getEntityKey())))
                                                        .collect(Collectors.toList())
                                       : Collections.emptyList();
    }

    @Override
    protected void persistEntity(Entity entity) {
        calcService.persistsInputFormValues(calculation, entityMeta, Collections.singletonList(entity));
    }

    @Override
    protected LocalDateTime getActuality() {
        return calculation.getModel().getActuality();
    }
}
