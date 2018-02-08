package ru.masterdm.crs.integration.structure.imprt.spi;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.masterdm.crs.integration.ModuleMetadata;
import ru.masterdm.crs.integration.spi.ModuleInfo;
import ru.masterdm.crs.integration.structure.imprt.StructureImportSyncCommandProcessor;

/**
 * {@link ModuleInfo} implementation to support SPI architecture.
 * @author Alexey Chalov
 */
public class StructureImportModuleInfo implements ModuleInfo {

    private static Properties moduleProperties;
    private static final String CONFIG_PROPERTIES = "structure-import-config.properties";

    private static final Logger LOG = LoggerFactory.getLogger(StructureImportModuleInfo.class);

    static {
        initProperties();
    }

    @Override
    public ModuleMetadata getMetadata() {
        return new ModuleMetadata(
                moduleProperties.getProperty("project.structureImport.name"),
                moduleProperties.getProperty("project.structureImport.version")
        );
    }

    @Override
    public String getPropertyFileName() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public String getSyncCommandProcessorBeanName() {
        return StructureImportSyncCommandProcessor.NAME;
    }

    /**
     * Initializes module properties.
     */
    private static void initProperties() {
        moduleProperties = new Properties();
        try {
            moduleProperties.load(StructureImportModuleInfo.class.getClassLoader().getResourceAsStream(CONFIG_PROPERTIES));
        } catch (IOException e) {
            LOG.error("Error initializing module properties.", e);
        }
    }
}
