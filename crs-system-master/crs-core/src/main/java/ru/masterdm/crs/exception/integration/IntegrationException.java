package ru.masterdm.crs.exception.integration;

import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.exception.ErrorCodeSupport;

/**
 * Integration exception.
 * @author Sergey Valiev
 */
public class IntegrationException extends CrsException implements ErrorCodeSupport<IntegrationErrorCode> {

    private IntegrationErrorCode integrationErrorCode;
    private String integrationModule;

    /**
     * Constructor.
     * @param integrationErrorCode integration error code
     * @param integrationModule name of integration module
     */
    public IntegrationException(IntegrationErrorCode integrationErrorCode, String integrationModule) {
        super(integrationErrorCode.name());
        this.integrationModule = integrationModule;
        this.integrationErrorCode = integrationErrorCode;
    }

    /**
     * Constructor.
     * @param msg exception message
     * @param integrationErrorCode integration error code
     * @param integrationModule name of integration module
     */
    public IntegrationException(String msg, IntegrationErrorCode integrationErrorCode, String integrationModule) {
        super(msg);
        this.integrationModule = integrationModule;
        this.integrationErrorCode = integrationErrorCode;
    }

    /**
     * Constructor.
     * @param msg exception message
     * @param throwable exception
     * @param integrationErrorCode integration error code
     */
    public IntegrationException(String msg, Throwable throwable, IntegrationErrorCode integrationErrorCode) {
        super(msg, throwable);
        this.integrationErrorCode = integrationErrorCode;
    }

    @Override
    public IntegrationErrorCode getErrorCode() {
        return integrationErrorCode;
    }

    /**
     * Returns name of integration module.
     * @return name of integration module
     */
    public String getIntegrationModule() {
        return integrationModule;
    }
}
