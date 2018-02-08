package ru.masterdm.crs.integration.client.portal.service;

/**
 * Client portal integration service.
 * @author Sergey Valiev
 */
public interface ClientPortalIntegrationService {

    /**
     * Changes cron.
     * @throws Exception of error rise
     */
    void changeCron() throws Exception;

    /**
     * Starts synchronization.
     */
    void startSynchronization();

    /**
     * Returns cron expression for scheduled tasks.
     * @return cron expression
     */
    String getCron();

    /**
     * Returns timer name.
     * @return timer name
     */
    String getTimerName();

    /**
     * Returns timer group name.
     * @return timer group name
     */
    String getTimerGroupName();
}
