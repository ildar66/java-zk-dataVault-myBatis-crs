package ru.masterdm.crs.integration.client.portal.service;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;

import org.apache.camel.CamelContext;
import org.apache.camel.component.quartz2.QuartzComponent;
import org.quartz.CronTrigger;
import org.quartz.impl.StdScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import ru.masterdm.crs.integration.client.portal.dao.ClientPortalIntegrationDao;

/**
 * Client portal integration service implementation.
 * @author Alexey Chalov
 */
@Service("clientPortalIntegrationService")
public class ClientPortalIntegrationServiceImpl implements ClientPortalIntegrationService {

    @Autowired
    private ClientPortalIntegrationDao clientPortalIntegrationDao;
    @Autowired(required = false)
    @Qualifier("clientPortalIntegrationContext")
    private CamelContext camelContext;

    private static final Logger LOG = LoggerFactory.getLogger(ClientPortalIntegrationServiceImpl.class);

    @Override
    public void changeCron() throws Exception {
        QuartzComponent quartz = (QuartzComponent) camelContext.getComponent("quartz2");
        StdScheduler stdScheduler = (StdScheduler) quartz.getScheduler();
        CronTrigger cronTrigger = newTrigger().withIdentity(getTimerName(), getTimerGroupName())
                                              .withSchedule(cronSchedule(getCron().replaceAll("\\+", " ")))
                                              .build();
        stdScheduler.rescheduleJob(triggerKey(getTimerName(), getTimerGroupName()), cronTrigger);
        LOG.debug("Changed cron to " + cronTrigger.getCronExpression());
    }

    @Override
    public void startSynchronization() {
        LOG.debug("Started synchronization process.");
        clientPortalIntegrationDao.startSynchronization();
        LOG.debug("Completed synchronization process.");
    }

    @Override
    public String getCron() {
        return clientPortalIntegrationDao.getCron();
    }

    @Override
    public String getTimerName() {
        return "clientPortalTimer";
    }

    @Override
    public String getTimerGroupName() {
        return "clientPortalTimerGroup";
    }
}
