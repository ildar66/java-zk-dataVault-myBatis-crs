package ru.masterdm.crs;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import ru.masterdm.crs.service.SecurityService;

/**
 * Security test context configuration.
 * @author Sergey Valiev
 */
public class SecurityContextBeanPostProcessor implements BeanPostProcessor {

    public static final String ADMINWF_LOGIN = "adminwf";

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof SecurityService) {
            ((SecurityService) bean).defineSecurityContext(ADMINWF_LOGIN);
        }
        return bean;
    }
}
