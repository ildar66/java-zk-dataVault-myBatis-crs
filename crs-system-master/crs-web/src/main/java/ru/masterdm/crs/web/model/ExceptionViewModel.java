package ru.masterdm.crs.web.model;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Include;

import ru.masterdm.crs.web.domain.UserProfile;
import ru.masterdm.crs.web.exception.ExceptionEnvelope;
import ru.masterdm.crs.web.exception.ExceptionHandler;
import ru.masterdm.crs.web.exception.ExceptionHandlerClassifier;

/**
 * Exception view model.
 * @author Alexey Kirilchev
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ExceptionViewModel {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionViewModel.class);

    private static final String EXCEPTION_KEY = "javax.servlet.error.exception";
    private static final String STATUS_CODE_KEY = "javax.servlet.error.status_code";
    private static final String REQUEST_URI_KEY = "javax.servlet.error.request_uri";
    private static final String PAGE_INCLUDE = "pageInclude";

    @WireVariable("userProfile")
    private UserProfile userProfile;
    @WireVariable("config")
    private Properties config;
    @WireVariable("webConfig")
    private Properties webConfig;

    private String fileName;
    private String stackTrace;
    private ExceptionEnvelope exceptionEnvelope;

    /**
     * Initiates context.
     * @param currentPage current page
     * @param exception exception
     */
    @Init
    public void init(@ContextParam(ContextType.PAGE) Page currentPage, @ExecutionParam(EXCEPTION_KEY) Throwable exception) {
        LocalDateTime dateNow = LocalDateTime.now();
        ExceptionHandler handler = ExceptionHandlerClassifier.getByException(exception);
        if (handler != null) {
            exceptionEnvelope = handler.getExceptionEnvelope(exception, userProfile);
        } else {
            exceptionEnvelope = new ExceptionEnvelope(exception.getMessage());
        }
        stackTrace = getStackTrace(dateNow, exception.getMessage(), currentPage);
        fileName = String.format(webConfig.getProperty("error.file.name.format"), dateNow);
        LOG.error(stackTrace);
    }

    /**
     * Returns stackTrace.
     * @return stackTrace
     */
    public String getStackTrace() {
        return stackTrace;
    }

    /**
     * Returns exception envelope.
     * @return exception envelope
     */
    public ExceptionEnvelope getExceptionEnvelope() {
        return exceptionEnvelope;
    }

    /**
     * Download stack trace.
     */
    @Command
    public void downloadStackTrace() {
        Filedownload.save(stackTrace, webConfig.getProperty("error.file.mime"), fileName);
    }

    /**
     * Reload window.
     */
    @Command
    public void reload() {
        Executions.sendRedirect(null);
    }

    /**
     * Detaches window.
     * @param view view
     */
    @Command
    public void closeDialogWindow(@ContextParam(ContextType.VIEW) Component view) {
        view.detach();
    }

    /**
     * Returns stacktrace.
     * @param dateNow current date and time
     * @param errorMessage error message
     * @param currentPage current page
     * @return stacktrace
     */
    private String getStackTrace(LocalDateTime dateNow, String errorMessage, Page currentPage) {
        Execution execution = Executions.getCurrent();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("Status code: " + execution.getAttribute(STATUS_CODE_KEY));
        try {
            pw.println(String.format("Application version: %s/%s %s", config.getProperty("project.version"), config.getProperty("project.build.time"),
                                     config.getProperty("build.branch")));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        try {
            pw.println("Server Url: " + new URL(execution.getScheme(), execution.getServerName(), execution.getServerPort(), ""));
        } catch (MalformedURLException e) {
            LOG.error(e.getMessage(), e);
        }
        try {
            Include includedPage = (Include) currentPage.getFellow(PAGE_INCLUDE);
            pw.println("Included page: " + (includedPage != null ? includedPage.getSrc() : null));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        try {
            pw.println("login: " + userProfile.getLogin());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        pw.println("Date: " + dateNow.format(DateTimeFormatter.ofPattern(Labels.getLabel("date_time_second_format"))));
        pw.println("Error header: " + errorMessage);
        pw.println("Stack trace: ");
        pw.println(ExceptionUtils.getStackTrace((Throwable) execution.getAttribute(EXCEPTION_KEY)));
        return sw.toString();
    }
}
