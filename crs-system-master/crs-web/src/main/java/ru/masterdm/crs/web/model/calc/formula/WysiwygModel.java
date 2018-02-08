package ru.masterdm.crs.web.model.calc.formula;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Textbox;

/**
 * Wysiwyg component model class.
 * @author Vladimir Shvets
 */
public class WysiwygModel {

    private static final String MACRO_APPLY = "apply_wysiwyg";

    @Wire("#ww_code")
    private Textbox codeTextbox;

    /**
     * After compose event.
     * @param view Component view
     */
    @AfterCompose
    public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireComponents(view, this, false);
        Selectors.wireEventListeners(view, this);
        String js = String.format("%s('%s')", MACRO_APPLY, codeTextbox.getUuid());
        Clients.evalJavaScript(js);
    }

    /**
     * Updates value.
     * @param event event
     */
    @Command
    @NotifyChange({"*"})
    public void update(@BindingParam("event") Event event) {
        codeTextbox.setValue(event.getData().toString());
    }

}
