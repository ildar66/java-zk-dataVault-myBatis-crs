package ru.masterdm.crs.integration.spi;

import ru.masterdm.crs.integration.ModuleMetadata;

/**
 * SPI interface for registering integration modules.
 * @author Alexey Chalov
 */
public interface ModuleInfo {

    /**
     * Returns module metadata object.
     * @return {@link ModuleMetadata}
     */
    ModuleMetadata getMetadata();

    /**
     * Returns configuration property file name.
     * @return configuration property file name
     */
    String getPropertyFileName();

    /**
     * Returns synchronous command processor bean name.
     * @return synchronous command processor bean name
     */
    String getSyncCommandProcessorBeanName();
}
