package ru.masterdm.crs.integration.structure.export.spi;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.masterdm.crs.integration.ModuleMetadata;
import ru.masterdm.crs.integration.spi.ModuleInfo;
import ru.masterdm.crs.integration.structure.export.StructureExportSyncCommandProcessor;

/**
 * {@link ModuleInfo} implementation to support SPI architecture.
 * @author Alexey Chalov
 */
public class StructureExportModuleInfo implements ModuleInfo {

    private static Properties moduleProperties;
    private static final String CONFIG_PROPERTIES = "structure-export-config.properties";

    private static final Logger LOG = LoggerFactory.getLogger(StructureExportModuleInfo.class);

    static {
        initProperties();
    }

    @Override
    public ModuleMetadata getMetadata() {
        return new ModuleMetadata(
                moduleProperties.getProperty("project.structureExport.name"),
                moduleProperties.getProperty("project.structureExport.version")
        );
    }

    @Override
    public String getPropertyFileName() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public String getSyncCommandProcessorBeanName() {
        return StructureExportSyncCommandProcessor.NAME;
    }

    /**
     * Initializes module properties.
     */
    private static void initProperties() {
        moduleProperties = new Properties();
        try {
            moduleProperties.load(StructureExportModuleInfo.class.getClassLoader().getResourceAsStream(CONFIG_PROPERTIES));
        } catch (IOException e) {
            LOG.error("Error initializing module properties.", e);
        }
    }
}
