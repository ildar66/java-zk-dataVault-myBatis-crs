package ru.masterdm.crs.integration.rating.imp.spi;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.masterdm.crs.integration.ModuleMetadata;
import ru.masterdm.crs.integration.rating.imp.RatingImportSyncCommandProcessor;
import ru.masterdm.crs.integration.spi.ModuleInfo;

/**
 * {@link ModuleInfo} implementation to support SPI architecture.
 * @author Alexey Chalov
 */
public class RatingImportModuleInfo  implements ModuleInfo {

    private static Properties moduleProperties;
    private static final String CONFIG_PROPERTIES = "rating-import-config.properties";

    private static final Logger LOG = LoggerFactory.getLogger(RatingImportModuleInfo.class);

    static {
        initProperties();
    }

    @Override
    public ModuleMetadata getMetadata() {
        return new ModuleMetadata(
                moduleProperties.getProperty("project.ratingImport.name"),
                moduleProperties.getProperty("project.ratingImport.version")
        );
    }

    @Override
    public String getPropertyFileName() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public String getSyncCommandProcessorBeanName() {
        return RatingImportSyncCommandProcessor.NAME;
    }

    /**
     * Initializes module properties.
     */
    private static void initProperties() {
        moduleProperties = new Properties();
        try {
            moduleProperties.load(RatingImportModuleInfo.class.getClassLoader().getResourceAsStream(CONFIG_PROPERTIES));
        } catch (IOException e) {
            LOG.error("Error initializing module properties.", e);
        }
    }
}
