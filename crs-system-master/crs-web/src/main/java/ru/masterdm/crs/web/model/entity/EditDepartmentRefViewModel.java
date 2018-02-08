package ru.masterdm.crs.web.model.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.SmartNotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModelList;

import ru.masterdm.crs.domain.entity.Department;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.attribute.EntityAttribute;
import ru.masterdm.crs.domain.integration.CpiDepartment;
import ru.masterdm.crs.service.IntegrationService;
import ru.masterdm.crs.web.domain.UserProfile;
import ru.masterdm.crs.web.domain.entity.EntityStatus;

/**
 * Edit department ref view model class.
 * @author Igor Matushak
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EditDepartmentRefViewModel {

    @WireVariable
    private IntegrationService integrationService;
    @WireVariable("userProfile")
    private UserProfile userProfile;

    private EntityStatus entityStatus;
    private boolean isEdit;

    private ListModelList<Entity> candidateModel;
    private ListModelList<EntityAttribute> chosenModel;
    private List<CpiDepartment> choosenEntities;
    private String entityFilter;

    /**
     * Initiates context.
     * @param entityStatus entity status
     * @param isEdit is edit
     */
    @Init
    public void initSetup(@ExecutionArgParam("entityStatus") EntityStatus entityStatus,
                          @ExecutionArgParam("isEdit") Boolean isEdit) {
        this.entityStatus = entityStatus;
        this.isEdit = isEdit;
    }

    /**
     * Returns candidate cpi departments.
     * @return candidate cpi departments
     */
    public List<CpiDepartment> getCandidateEntities() {
        return integrationService.getCpiDepartments(entityFilter, userProfile.getLocale());
    }

    /**
     * Returns chosen cpi departments.
     * @return chosen cpi department
     */
    public List<CpiDepartment> getChosenEntities() {
        if (choosenEntities == null) {
            List<Pair<Department, List<CpiDepartment>>> departmentMappings = integrationService.getCpiDepartmentMappings();
            Pair<Department, List<CpiDepartment>> cpiDepartmentPair = departmentMappings.stream()
                                                                                        .filter(p -> p.getLeft().equals(entityStatus.getEntity()))
                                                                                        .findFirst().orElse(null);
            choosenEntities = (cpiDepartmentPair == null) ? new ArrayList<>() : cpiDepartmentPair.getRight();
        }
        return choosenEntities;
    }

    /**
     * Detaches window.
     * @param view view
     */
    @Command
    public void detachWindow(@ContextParam(ContextType.VIEW) Component view) {
        view.detach();
    }

    /**
     * Saves entity.
     * @param view view
     */
    @Command
    public void saveEntity(@ContextParam(ContextType.VIEW) Component view) {
        integrationService.persistDepartmentMappings((Department) entityStatus.getEntity(), choosenEntities);
        BindUtils.postGlobalCommand(null, null, "entitiesRefresh", null);
        BindUtils.postGlobalCommand(null, null, "entityRefresh", null);
        view.detach();
    }

    /**
     * Chooses.
     */
    @Command
    @SmartNotifyChange("*")
    public void choose() {
        for (CpiDepartment entity : (Set<CpiDepartment>) getCandidateModel().getSelection()) {
            if (choosenEntities.stream().filter(p -> p.getId().equals(entity.getId())).count() == 0) {
                choosenEntities.add(entity);
            }
        }
        chosenModel = null;
    }

    /**
     * Removes all.
     */
    @SmartNotifyChange("*")
    @Command
    public void removeAll() {
        choosenEntities.clear();
        chosenModel = null;
    }

    /**
     * Removes.
     */
    @SmartNotifyChange("*")
    @Command
    public void remove() {
        choosenEntities.removeAll(choosenEntities
                                          .stream()
                                          .filter(ea -> ((Set<CpiDepartment>) getChosenModel().getSelection()).stream().anyMatch(s -> s.equals(ea)))
                                          .collect(Collectors.toList()));
        chosenModel = null;
    }

    /**
     * Returns entity status.
     * @return entity status
     */
    public EntityStatus getEntityStatus() {
        return entityStatus;
    }

    /**
     * Sets entity status.
     * @param entityStatus entity status
     */
    public void setEntityStatus(EntityStatus entityStatus) {
        this.entityStatus = entityStatus;
    }

    /**
     * Returns is edit.
     * @return is edit
     */
    public boolean isEdit() {
        return isEdit;
    }

    /**
     * Changes filter.
     */
    @Command
    @SmartNotifyChange("candidateModel")
    public void changeFilter() {
        candidateModel = null;
    }

    /**
     * Returns candidate model.
     * @return candidate model
     */
    public ListModelList getCandidateModel() {
        if (candidateModel == null) {
            candidateModel = new ListModelList(getCandidateEntities());
            candidateModel.setMultiple(true);
        }
        return candidateModel;
    }

    /**
     * Returns chosen model.
     * @return chosen model
     */
    public ListModelList getChosenModel() {
        if (chosenModel == null) {
            chosenModel = new ListModelList(getChosenEntities());
            chosenModel.setMultiple(true);
        }
        return chosenModel;
    }

    /**
     * Returns entity filter.
     * @return entity filter
     */
    public String getEntityFilter() {
        return entityFilter;
    }

    /**
     * Sets entity filter.
     * @param entityFilter entity filter
     */
    public void setEntityFilter(String entityFilter) {
        this.entityFilter = entityFilter;
    }
}
