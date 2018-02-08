package ru.masterdm.crs.web.model.entity;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Window;

import ru.masterdm.crs.domain.entity.Department;
import ru.masterdm.crs.domain.entity.criteria.Conjunction;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.Where;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.integration.IntegrationModuleName;
import ru.masterdm.crs.service.IntegrationService;
import ru.masterdm.crs.web.domain.entity.EntityStatus;

/**
 * Edit departments view model class.
 * @author Igor Matushak
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EditDepartmentsViewModel extends EditEntitiesViewModel {

    @WireVariable
    private IntegrationService integrationService;

    /**
     * Initiates context.
     */
    @Override
    @Init
    public void initSetup() {
        super.initSetup();
    }

    @Override
    protected void filterByText(Where where, String filter) {
        EntityMeta departmentMeta = entityMetaService.getEntityMetaByKey(Department.METADATA_KEY, null);
        AttributeMeta nameMeta = departmentMeta.getAttributeMetadata(Department.DepartmentAttributeMeta.NAME.getKey());
        AttributeMeta fullNameMeta = departmentMeta.getAttributeMetadata(Department.DepartmentAttributeMeta.FULL_NAME.getKey());

        where.addItem(new WhereItem(departmentMeta.getKeyAttribute(), Operator.LIKE, "%" + filter + "%"));
        where.addItem(new WhereItem(Conjunction.OR, nameMeta, Operator.LIKE, "%" + filter + "%"));
        where.addItem(new WhereItem(Conjunction.OR, fullNameMeta, Operator.LIKE, "%" + filter + "%"));
    }

    @Override
    public Boolean getEntitySupportsDepartmentMapping() {
        return integrationService.isModuleAvailable(IntegrationModuleName.CLIENT_PORTAL.getModuleName());
    }

    /**
     * Edits department ref.
     * @param entityStatus entity status
     * @param isEdit isEdit
     */
    @Command
    public void editDepartmentRef(@BindingParam("entityStatus") EntityStatus entityStatus,
                                  @BindingParam("isEdit") boolean isEdit) {
        Map<String, Object> map = new HashMap<>();
        map.put("entityStatus", entityStatus);
        map.put("isEdit", isEdit);
        Window window = (Window) Executions.createComponents(pages.getProperty("entity.edit_department_ref"), null, map);
        window.doModal();
    }
}
