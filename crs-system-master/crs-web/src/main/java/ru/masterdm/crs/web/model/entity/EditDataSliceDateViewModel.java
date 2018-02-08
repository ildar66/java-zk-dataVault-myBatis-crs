package ru.masterdm.crs.web.model.entity;

import java.time.LocalDateTime;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import ru.masterdm.crs.web.domain.DataSlice;

/**
 * Edit data slice date view model class.
 * @author Igor Matushak
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EditDataSliceDateViewModel {

    @WireVariable("dataSlice")
    private DataSlice dataSlice;

    private String entityMetaKey;
    private int selectedIndex;
    private LocalDateTime dataSliceDate;

    /**
     * Initiates context.
     * @param entityMetaKey entity meta key
     */
    @Init
    public void initSetup(@ExecutionArgParam("entityMetaKey") String entityMetaKey) {
        this.entityMetaKey = entityMetaKey;
        this.dataSliceDate = dataSlice.getDataSliceDateByKey(entityMetaKey);
        selectedIndex = (dataSliceDate == null ? 0 : 1);
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
     * Saves data slice.
     * @param view view
     */
    @Command
    public void saveDataSlice(@ContextParam(ContextType.VIEW) Component view) {
        view.detach();
        dataSlice.setDataSliceDateByKey(entityMetaKey, selectedIndex == 0 ? null : dataSliceDate);
        BindUtils.postGlobalCommand(null, null, "resetDateSliceDate", null);
    }

    /**
     * Returns selected index.
     * @return selected index
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * Sets selected index.
     * @param selectedIndex selected index
     */
    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    /**
     * Returns data slice date.
     * @return data slice date
     */
    public LocalDateTime getDataSliceDate() {
        return dataSliceDate;
    }

    /**
     * Sets data slice date.
     * @param dataSliceDate data slice date
     */
    public void setDataSliceDate(LocalDateTime dataSliceDate) {
        this.dataSliceDate = dataSliceDate;
    }

    /**
     * Returns data slice date constraint.
     * @return data slice date constraint
     */
    public String getDataSliceDateConstraint() {
        return selectedIndex == 1 ? "no empty" : null;
    }

    /**
     * On check.
     */
    @Command
    public void onCheck() {
        if (selectedIndex == 1 && dataSliceDate == null) {
            dataSliceDate = LocalDateTime.now();
        }
        BindUtils.postNotifyChange(null, null, this, "*");
    }
}
