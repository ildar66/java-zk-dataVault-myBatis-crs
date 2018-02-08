package ru.masterdm.crs.web.model.audit;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.annotation.SmartNotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Window;

import ru.masterdm.crs.domain.AuditAction;
import ru.masterdm.crs.domain.AuditLog;
import ru.masterdm.crs.domain.AuditLog.AuditLogFilter;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.service.AuditService;
import ru.masterdm.crs.web.domain.UserProfile;

/**
 * Audit Log View Model.
 * @author Kuzmin Mikhail
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class AuditLogViewModel {

    @WireVariable
    private AuditService auditService;
    @WireVariable("webConfig")
    private Properties webConfig;
    @WireVariable("pages")
    private Properties pages;
    @WireVariable("userProfile")
    private UserProfile userProfile;

    private int pageSize;
    private int activePage = 0;
    private long totalSize;
    private LocalDateTime dateToFilter;
    private LocalDateTime dateFromFilter;

    private ListModelList<Pair<AuditLogFilter, Object>> auditLogFilters = new ListModelList<>();

    private ListModelList<AuditLog> auditLogs;

    /**
     * Initiates context.
     */
    @Init
    public void initSetup() {
        auditLogFilters.setMultiple(true);
        pageSize = Integer.parseInt(webConfig.getProperty("pageSize"));
    }

    /**
     * Reset paging.
     */
    private void resetPaging() {
        activePage = 0;
    }

    /**
     * Returns total size.
     * @return total size
     */
    public long getTotalSize() {
        return totalSize;
    }

    /**
     * Returns page size.
     * @return page size
     */
    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * Returns Active Page.
     * @return activePage
     */
    public int getActivePage() {
        return activePage;
    }

    /**
     * Sets active page.
     * @param activePage active page
     */
    @NotifyChange({"model"})
    public void setActivePage(int activePage) {
        this.activePage = activePage;
        auditLogs = null;
    }

    /**
     * Returns model.
     * @return model
     */
    public ListModelList<AuditLog> getModel() {
        if (auditLogs == null) {
            auditLogs = new ListModelList(getAuditLogs());
        }
        return auditLogs;
    }

    /**
     * Returns audit logs.
     * @return audit logs
     */
    private List<AuditLog> getAuditLogs() {
        RowRange rowRange = RowRange.newAsPageAndSize(activePage, pageSize);
        List<AuditLog> auditLogList = auditService.getLogs(getFiltersForService(auditLogFilters), rowRange);
        totalSize = rowRange.getTotalCount();
        return auditLogList;
    }

    /**
     * Returns audit log filters.
     * @return audit log filters
     */
    public ListModelList<Pair<AuditLog.AuditLogFilter, Object>> getAuditLogFilters() {
        return auditLogFilters;
    }

    /**
     * Edits audit filter.
     */
    @Command
    public void editAuditFilter() {
        Map<String, Object> map = new HashMap<>();
        map.put("savedFilters", auditLogFilters);
        Window window = (Window) Executions.createComponents(pages.getProperty("audit.audit_logs_filter"), null, map);
        window.doModal();
    }

    /**
     * Removes audit filter.
     * @param event audit log filter
     */
    @Command
    @SmartNotifyChange("*")
    public void deleteAuditFilter(@BindingParam("event") SelectEvent event) {
        Pair filterToRemove = new MutablePair<>(null, null);
        for (Pair lf : auditLogFilters) {
            if (!event.getSelectedObjects().contains(lf))
                filterToRemove = lf;
        }
        auditLogFilters.remove(filterToRemove);
        auditLogs = null;
        resetPaging();
        BindUtils.postNotifyChange(null, null, this, "activePage");
        BindUtils.postNotifyChange(null, null, this, "totalSize");
    }

    /**
     * Refreshes audit logs.
     */
    @GlobalCommand
    @SmartNotifyChange("*")
    public void auditLogsRefresh() {
        auditLogFilters.forEach(lf -> auditLogFilters.addToSelection(lf));
        auditLogs = null;
        BindUtils.postNotifyChange(null, null, this, "model");
        BindUtils.postNotifyChange(null, null, this, "auditLogFilters");
    }

    /**
     * Returns label for AuditAction value.
     * @param action auditAction
     * @return label
     */
    public String getActionName(AuditAction action) {
        return Labels.getLabel("audit_logs_" + action.toString().toLowerCase());
    }

    /**
     * Returns object description for listbox.
     * @param auditLog audit log
     * @return description
     */
    public String getObjectDescr(AuditLog auditLog) {
        return Labels.getLabel("audit_logs_object_desc",
                               new Object[] {
                                       auditLog.getEntity().getMeta().getKeyName().getDescription(userProfile.getLocale()),
                                       auditLog.getEntity().getKey()
        });
    }

    /**
     * Returns filter list for service.
     * @param auditLogFilters audit log filters
     * @return filter list
     */
    private Map<String, Object> getFiltersForService(ListModelList<Pair<AuditLogFilter, Object>> auditLogFilters) {
        Map<String, Object> filterList = new HashMap<>();
        auditLogFilters.forEach(pair -> filterList.put(pair.getLeft().toString(), pair.getRight()));
        resetPaging();
        filterList.remove(AuditLogFilter.DATE_TO.name());
        filterList.remove(AuditLogFilter.DATE_FROM.name());
        if (dateToFilter != null)
            filterList.put(AuditLogFilter.DATE_TO.name(), dateToFilter);
        if (dateFromFilter != null)
            filterList.put(AuditLogFilter.DATE_FROM.name(), dateFromFilter);
        return filterList;
    }

    /**
     * Returns label for AuditLogFilter value.
     * @param auditLogFilter Audit Log Filter
     * @return label
     */
    public String getAuditLogFilterName(Pair<AuditLogFilter, Object> auditLogFilter) {
        String resultString = Labels.getLabel("audit_logs_" + auditLogFilter.getLeft().toString().toLowerCase());
        if (auditLogFilter.getRight() != null)
            if (auditLogFilter.getRight() instanceof AuditAction) {
                resultString += " " + Labels.getLabel("audit_logs_" + auditLogFilter.getRight().toString().toLowerCase());
            } else
                resultString += " " + auditLogFilter.getRight().toString();
        return resultString;
    }

    /**
     * Returns dateToFilter.
     * @return filter
     */
    public LocalDateTime getDateToFilter() {
        return dateToFilter;
    }

    /**
     * Sets dateToFilter.
     * @param dateToFilter filter
     */
    public void setDateToFilter(LocalDateTime dateToFilter) {
        this.dateToFilter = dateToFilter;
    }

    /**
     * Returns dateFromFilter.
     * @return filter
     */
    public LocalDateTime getDateFromFilter() {
        return dateFromFilter;
    }

    /**
     * Sets dateFromFilter.
     * @param dateFromFilter filter
     */
    public void setDateFromFilter(LocalDateTime dateFromFilter) {
        this.dateFromFilter = dateFromFilter;
    }
}
