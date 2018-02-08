package liquibase.integration.spring;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

/**
 * Multiple liquibase changelog support class.
 * @author Alexey Chalov
 */
public class MultipleChangeLogSupport implements ApplicationContextAware {

    private String changeLogs;
    private DataSource dataSource;
    private ApplicationContext applicationContext;

    /**
     * Applies changelogs to database.
     * @throws IOException if error rise
     */
    @PostConstruct
    private void initializeLiqubaseBeans() throws IOException {
        AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
        for (Resource resource :  applicationContext.getResources(changeLogs)) {
            SpringLiquibase liqubaseBean = new SpringLiquibase();
            liqubaseBean.setDataSource(dataSource);
            liqubaseBean.setChangeLog(resource.getURI().toString());
            beanFactory.initializeBean(liqubaseBean, SpringLiquibase.class.getSimpleName());
            beanFactory.destroyBean(liqubaseBean);
        }
        beanFactory.destroyBean(this);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Sets changelogs.
     * @param changeLogs changelogs
     */
    public void setChangeLogs(String changeLogs) {
        this.changeLogs = changeLogs;
    }

    /**
     * Sets {@link DataSource} instance.
     * @param dataSource {@link DataSource} instance
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
