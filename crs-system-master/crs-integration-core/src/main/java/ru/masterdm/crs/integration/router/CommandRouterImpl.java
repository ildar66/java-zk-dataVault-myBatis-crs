package ru.masterdm.crs.integration.router;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import ru.masterdm.crs.integration.ExecutionCommand;
import ru.masterdm.crs.integration.ExecutionCommandResult;
import ru.masterdm.crs.integration.ModuleMetadata;
import ru.masterdm.crs.integration.spi.ModuleInfo;

/**
 * {@link CommandRouter} service implementation.
 * @author Alexey Chalov
 */
@Service("commandRouter")
public class CommandRouterImpl implements CommandRouter {

    private Map<String, ModuleInfo> modules;
    private List<String> combinedModulePropertyFileNames;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    @EndpointInject(context = "commandRouterContext", uri = "direct:routeCommand")
    private ProducerTemplate template;

    private static final Logger LOG = LoggerFactory.getLogger(CommandRouterImpl.class);

    @Override
    public void route(ExecutionCommand command) {
        template.sendBody(command);
    }

    @Override
    public ExecutionCommandResult routeSync(ExecutionCommand command) {
        String processorName = modules.get(command.getModule()).getSyncCommandProcessorBeanName();
        SyncCommandProcessor processor = null;
        try {
            processor = (SyncCommandProcessor) applicationContext.getBean(processorName);
        } catch (Exception e) {
            LOG.warn("No sync executor found for module " + command.getModule());
        }
        if (processor == null) {
            return null;
        }
        return processor.execute(command);
    }

    @Override
    public Map<String, ModuleMetadata> getModules() {
        return modules.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getMetadata()));
    }

    @Override
    public String[] getModulePropertyFileNames() {
        return combinedModulePropertyFileNames.toArray(new String[combinedModulePropertyFileNames.size()]);
    }

    /**
     * Initializes integration module data.
     */
    @PostConstruct
    public void afterPropertiesSet() {
        ServiceLoader<ModuleInfo> loader = ServiceLoader.load(ModuleInfo.class);
        modules = new HashMap<>();
        combinedModulePropertyFileNames = new ArrayList<>();
        Iterator<ModuleInfo> iterator = loader.iterator();
        while (iterator.hasNext()) {
            ModuleInfo moduleInfo = iterator.next();
            modules.put(moduleInfo.getMetadata().getName(), moduleInfo);
            combinedModulePropertyFileNames.add(moduleInfo.getPropertyFileName());
        }
    }
}
