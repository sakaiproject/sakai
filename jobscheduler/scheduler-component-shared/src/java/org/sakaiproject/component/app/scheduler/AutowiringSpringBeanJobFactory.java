package org.sakaiproject.component.app.scheduler;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * This JobFactory autowires automatically the created quartz bean with spring @Autowired dependencies.
 *
 * @author jelies (thanks to Brian Matthews)
 * @link http://webcache.googleusercontent.com/search?q=cache:FH-N1i--sDgJ:blog.btmatthews.com/2011/09/24/inject-application-context-dependencies-in-quartz-job-beans/+&cd=7&hl=en&ct=clnk&gl=es)
 */
public final class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory implements
        ApplicationContextAware {

    private transient AutowireCapableBeanFactory beanFactory;

    @Override
    public void setApplicationContext(final ApplicationContext context) {
        beanFactory = context.getAutowireCapableBeanFactory();
    }

    @Override
    protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
        final Object job = super.createJobInstance(bundle);
        beanFactory.autowireBean(job);
        return job;
    }
}