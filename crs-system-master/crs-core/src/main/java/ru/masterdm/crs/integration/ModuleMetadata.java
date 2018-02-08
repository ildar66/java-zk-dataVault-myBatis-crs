package ru.masterdm.crs.integration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Integration module metadata holder.
 * @author Alexey Chalov
 */
public class ModuleMetadata {

    private final String name;
    private final String version;

    /**
     * Constructor.
     * @param name module name
     * @param version module build version
     */
    @JsonCreator
    public ModuleMetadata(@JsonProperty("name") String name, @JsonProperty("version") String version) {
        this.name = name;
        this.version = version;
    }

    /**
     * Returns module name.
     * @return module name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns module build version.
     * @return module build version
     */
    public String getVersion() {
        return version;
    }
}
