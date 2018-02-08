package ru.masterdm.crs.integration.client.portal.spi;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.masterdm.crs.integration.ModuleMetadata;
import ru.masterdm.crs.integration.client.portal.service.ClientPortalIntegrationSyncCommandProcessor;
import ru.masterdm.crs.integration.spi.ModuleInfo;

/**
 * {@link ModuleInfo} implementation to support SPI architecture.
 * @author Alexey Chalov
 */
public class ClientPortalModuleInfo implements ModuleInfo {

    private static Properties moduleProperties;
    private static final String CONFIG_PROPERTIES = "client-portal-integration-config.properties";

    private static final Logger LOG = LoggerFactory.getLogger(ClientPortalModuleInfo.class);

    static {
        initProperties();
    }

    @Override
    public ModuleMetadata getMetadata() {
        return new ModuleMetadata(
                moduleProperties.getProperty("project.clientPortalIntegration.name"),
                moduleProperties.getProperty("project.clientPortalIntegration.version")
        );
    }

    @Override
    public String getPropertyFileName() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public String getSyncCommandProcessorBeanName() {
        return ClientPortalIntegrationSyncCommandProcessor.NAME;
    }

    /**
     * Initializes module properties.
     */
    private static void initProperties() {
        moduleProperties = new Properties();
        try {
            moduleProperties.load(ClientPortalModuleInfo.class.getClassLoader().getResourceAsStream(CONFIG_PROPERTIES));
        } catch (IOException e) {
            LOG.error("Error initializing module properties.", e);
        }
    }
}
