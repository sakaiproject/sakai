package org.sakaiproject.modi;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public interface BeanSource {
    public void registerBeans(BeanDefinitionRegistry registry);
}