package ru.masterdm.crs.web.util;

import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.metainfo.Property;
import org.zkoss.zk.ui.sys.IdGenerator;

/**
 * Html id generator class.
 * @author Igor Matushak
 */
public class IdGeneratorImpl implements IdGenerator {

    /**
     * Returns next component uuid.
     * @param desktop desktop
     * @param comp component
     * @param compInfo component info
     * @return next component uuid
     */
    public String nextComponentUuid(Desktop desktop, Component comp, ComponentInfo compInfo) {
        int i = Integer.parseInt(desktop.getAttribute("Id_Num").toString());
        i++;

        StringBuilder uuid = new StringBuilder("");

        desktop.setAttribute("Id_Num", String.valueOf(i));
        if (compInfo != null) {
            String id = getId(compInfo);
            if (id != null) {
                uuid.append(id).append("_");
            }
            String tag = compInfo.getTag();
            if (tag != null) {
                uuid.append(tag).append("_");
            }
        }
        return uuid.length() == 0 ? "zkcomp_" + i : uuid.append(i).toString();
    }

    /**
     * Returns identifier.
     * @param compInfo component info
     * @return identifier
     */
    public String getId(ComponentInfo compInfo) {
        List<Property> properties = compInfo.getProperties();
        for (Property property : properties) {
            if ("id".equals(property.getName())) {
                return property.getRawValue();
            }
        }
        return null;
    }

    /**
     * Returns next desktop identifier.
     * @param desktop desktop
     * @return next desktop identifier
     */
    public String nextDesktopId(Desktop desktop) {
        if (desktop.getAttribute("Id_Num") == null) {
            String number = "0";
            desktop.setAttribute("Id_Num", number);
        }
        return null;
    }

    /**
     * Returns next page uuid.
     * @param page page
     * @return next page uuid
     */
    public String nextPageUuid(Page page) {
        return null;
    }
}
