package ru.masterdm.crs.integration;

/**
 * Integration module names enumeration.
 * @author Alexey Chalov
 */
public enum IntegrationModuleName {

    CLIENT_PORTAL("crs-client-portal-integration");

    private final String name;

    /**
     * Constructor.
     * @param name integration module name
     */
    IntegrationModuleName(String name) {
        this.name = name;
    }

    /**
     * Returns integration module name.
     * @return integration module name
     */
    public String getModuleName() {
        return name;
    }
}
