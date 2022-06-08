package org.sakaiproject.modi;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public interface BeanDefinitionSource {
    public String getName();
    public void registerBeans(BeanDefinitionRegistry registry);
}