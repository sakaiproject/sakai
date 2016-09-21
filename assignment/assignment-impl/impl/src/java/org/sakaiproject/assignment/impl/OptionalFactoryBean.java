package org.sakaiproject.assignment.impl;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * This is used to handle option dependencies in spring XML.
 * This keeps the spring dependencies out of the class that actually needs the dependency.
 * @see <a href="http://stackoverflow.com/questions/3957430/optional-spring-bean-references">
 *     http://stackoverflow.com/questions/3957430/optional-spring-bean-references</a>
 */
public class OptionalFactoryBean extends AbstractFactoryBean<Object> implements BeanNameAware {

    private String beanName;

    @Override
    public void setBeanName(String beanName) {
        this.beanName = BeanFactoryUtils.originalBeanName(beanName);

    }

    @Override
    protected Object createInstance() throws Exception {
        if (getBeanFactory().containsBean(beanName)) {
            return getBeanFactory().getBean(beanName);
        } else {
            return null;
        }
    }

    @Override
    public Class<?> getObjectType() {
        return null;
    }
}
