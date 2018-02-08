package ru.masterdm.crs.web.domain;

/**
 * Class for keeping information about navigation path.
 * @author Vladimir Shvets
 */
public class BreadCrumb {

    private String page;
    private String name;
    private String key;

    /**
     * Constructor.
     * @param page page
     * @param name name
     */
    public BreadCrumb(String page, String name) {
        this.page = page;
        this.name = name;
    }

    /**
     * Constructor.
     * @param page page
     * @param name name
     * @param key entity key
     */
    public BreadCrumb(String page, String name, String key) {
        this(page, name);
        setKey(key);
    }

    /**
     * Returns page.
     * @return page
     */
    public String getPage() {
        return page;
    }

    /**
     * Sets page.
     * @param page page
     */
    public void setPage(String page) {
        this.page = page;
    }

    /**
     * Returns page name.
     * @return page name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets page name.
     * @param name page name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns entity key.
     * @return entity key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets entity key.
     * @param key entity key
     */
    public void setKey(String key) {
        this.key = key;
    }
}
