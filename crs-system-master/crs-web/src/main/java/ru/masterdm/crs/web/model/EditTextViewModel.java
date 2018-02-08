package ru.masterdm.crs.web.model;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;

/**
 * Edit text view model class.
 * @author Alexey Kirilchev
 */
public class EditTextViewModel {

    private String commandOnClose;
    private boolean isEdit;
    private String text;
    private String title;
    private Object rowElement;

    /**
     * Initiates context.
     * @param commandOnClose command to execute on close
     * @param text text
     * @param isEdit is edit
     * @param title title
     * @param rowElement row element, which field value changed
     */
    @Init
    public void initSetup(@ExecutionArgParam("commandOnClose") String commandOnClose,
                          @ExecutionArgParam("text") String text,
                          @ExecutionArgParam("isEdit") Boolean isEdit,
                          @ExecutionArgParam("title") String title,
                          @ExecutionArgParam("rowElement") Object rowElement) {
        this.commandOnClose = commandOnClose;
        this.text = text;
        this.isEdit = isEdit;
        this.rowElement = rowElement;
        this.title = title;
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
     * Saves text.
     * @param view view
     */
    @Command
    public void saveText(@ContextParam(ContextType.VIEW) Component view) {
        if (commandOnClose != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("text", text);
            map.put("rowElement", rowElement);
            BindUtils.postGlobalCommand(null, null, commandOnClose, map);
        }
        view.detach();
    }

    /**
     * Returns is edit.
     * @return is edit
     */
    public boolean isEdit() {
        return isEdit;
    }

    /**
     * Returns text.
     * @return text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets text.
     * @param text text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Returns title.
     * @return title
     */
    public String getTitle() {
        return title;
    }
}
