package org.sakaiproject.modi;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public interface BeanDefinitionSource {
    public void registerBeans(BeanDefinitionRegistry registry);
}