package ru.masterdm.crs.web.model.audit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.SmartNotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.ListModelList;

import ru.masterdm.crs.domain.AuditAction;
import ru.masterdm.crs.domain.AuditLog;

/**
 * Audit Log filter view model.
 * @author Kuzmin Mikhail
 */
public class AuditLogFilterViewModel {

    private ListModelList<Pair<AuditLog.AuditLogFilter, Object>> auditLogFilters;

    /**
     * Init context filters.
     * @param savedFilters audit log filters
     */
    @Init
    public void initSetup(@ExecutionArgParam("savedFilters") ListModelList<Pair<AuditLog.AuditLogFilter, Object>> savedFilters) {
        auditLogFilters = savedFilters;
    }

    /**
     * Detaches audit log filter window.
     * @param view view
     */
    @Command
    public void detachWindow(@ContextParam(ContextType.VIEW) Component view) {
        view.detach();
        BindUtils.postGlobalCommand(null, null, "auditLogsRefresh", null);
    }

    /**
     * Saves audit log filter.
     * @param view view
     */
    @Command
    public void saveAuditLogFilter(@ContextParam(ContextType.VIEW) Component view) {
        view.detach();
        BindUtils.postGlobalCommand(null, null, "auditLogsRefresh", null);
    }

    /**
     * Adds audit log filter.
     */
    @Command
    @SmartNotifyChange("*")
    public void addAuditLogFilter() {
        auditLogFilters.add(new MutablePair<>(AuditLog.AuditLogFilter.AUTHOR, null));
    }

    /**
     * Changes filter key.
     * @param auditLogFilter filter
     */
    @Command
    @SmartNotifyChange("*")
    public void changeFilterKey(@BindingParam("auditLogFilter") MutablePair<AuditLog.AuditLogFilter, Object> auditLogFilter) {
        auditLogFilter.setRight(null);
    }


    /**
     * Removes audit log filter.
     * @param auditLogFilter audit log filter
     */
    @Command
    @SmartNotifyChange("*")
    public void removeAuditLogFilter(@BindingParam("auditLogFilter") Pair<AuditLog.AuditLogFilter, Object> auditLogFilter) {
        auditLogFilters.remove(auditLogFilter);
    }

    /**
     * Returns audit log filters.
     * @return filters
     */
    public ListModelList<Pair<AuditLog.AuditLogFilter, Object>> getAuditLogFilters() {
        return auditLogFilters;
    }

    /**
     * Returns audit log filter list.
     * @return audit log filter list
     */
    public List<AuditLog.AuditLogFilter> getAuditLogFilterList() {
        List<AuditLog.AuditLogFilter> filterList = new ArrayList<>();
        Arrays.stream(AuditLog.AuditLogFilter.values())
                .filter(lf -> !lf.equals(AuditLog.AuditLogFilter.DATE_FROM) && !lf.equals(AuditLog.AuditLogFilter.DATE_TO))
                .forEach(filterList::add);
        return filterList;
    }

    /**
     * Returns audit log action list.
     * @return audit log action list
     */
    public List<AuditAction> getAuditLogActionlist() {
        return Arrays.asList(AuditAction.values());
    }

    /**
     * Returns label for AuditLogFilter value.
     * @param auditLogFilter Audit Log Filter
     * @return label
     */
    public String getAuditLogFilterName(AuditLog.AuditLogFilter auditLogFilter) {
        return Labels.getLabel("audit_logs_" + auditLogFilter.toString().toLowerCase());
    }

    /**
     * Returns label for AuditAction value.
     * @param action auditAction
     * @return label
     */
    public String getActionName(AuditAction action) {
        return Labels.getLabel("audit_logs_" + action.toString().toLowerCase());
    }
}
