package ru.masterdm.crs.web.model.form.constructor;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.zul.Window;

/**
 * View model for object dialog.
 * @author Vladimir Shvets
 */
public class CloneDialogViewModel {

    private int count = 1;

    /**
     * Returns count.
     * @return count
     */
    public int getCount() {
        return count;
    }

    /**
     * Sets count.
     * @param count count
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * Closes window by button.
     * @param window window
     */
    @Command
    public void close(@BindingParam("cmp") Window window) {
        Map params = new HashMap<>();
        params.put("count", count);
        BindUtils.postGlobalCommand(null, null, "cloneObjectAndRefresh", params);
        window.detach();
    }
}
