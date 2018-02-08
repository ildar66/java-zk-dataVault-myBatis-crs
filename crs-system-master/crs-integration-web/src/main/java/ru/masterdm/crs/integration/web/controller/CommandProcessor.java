package ru.masterdm.crs.integration.web.controller;

import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import ru.masterdm.crs.integration.ExecutionCommand;
import ru.masterdm.crs.integration.ModuleMetadata;
import ru.masterdm.crs.integration.router.CommandRouter;

/**
 * Integration service controller class.
 * @author Alexey Chalov
 */
@Controller
@RequestMapping("/")
public class CommandProcessor {

    @Autowired
    private CommandRouter router;

    private static final Logger LOG = LoggerFactory.getLogger(CommandProcessor.class);

    /**
     * Returns collection of registered integration modules.
     * @return collection of registered integration modules
     */
    @RequestMapping(value = "modules/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    Collection<ModuleMetadata> getModules() {
        return router.getModules().values();
    }

    /**
     * Starts asynchronous command execution.
     * @param command {@link ExecutionCommand} instance
     * @param response {@link HttpServletResponse} instance
     */
    @RequestMapping(value = "execute", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void executeCommand(@RequestBody ExecutionCommand command, HttpServletResponse response) {
        if (!router.getModules().keySet().contains(command.getModule())) {
            LOG.warn("Module = " + command.getModule() + " binding not found in current build.");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        LOG.debug("Received command: module = " + command.getModule() + ", operation = " + command.getOperation());
        router.route(command);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Performs synchronous command execution.
     * @param command {@link ExecutionCommand} instance
     * @param response {@link HttpServletResponse} instance
     * @return execution response
     */
    @RequestMapping(value = "executesync", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Object executeSyncCommand(@RequestBody ExecutionCommand command, HttpServletResponse response) {
        if (!router.getModules().keySet().contains(command.getModule())) {
            LOG.warn("Module = " + command.getModule() + " binding not found in current build.");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        LOG.debug("Received command: module = " + command.getModule() + ", operation = " + command.getOperation());
        Object result = router.routeSync(command);
        LOG.debug("Send result: " + result);
        response.setStatus(result != null ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST);
        return result;
    }
}
