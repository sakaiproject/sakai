/*
 * Copyright (c) 2003-2022 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.modi;

import org.sakaiproject.util.BeanFactoryPostProcessorCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractRefreshableApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * An {@link ApplicationContext} that can be shared across webapps as a parent. This serves as the main container for
 * Sakai, and the effective replacement for the {@link org.sakaiproject.component.api.ComponentManager}.
 * <p>
 * It loads beans indirectly, from sources that can yield definitions. This inversion allows components (or overrides,
 * or any source of beans) to be read or prepared in any format, rather than requiring XML files in a certain layout.
 */
public class SharedApplicationContext extends AbstractRefreshableApplicationContext {

    /** The ordered list of sources of bean definitions. */
    protected final List<BeanDefinitionSource> sources = new ArrayList<>();

    /**
     * Register a source of bean definitions with the context.
     * <p>
     * The sources will be invoked during refresh, in the {@link #loadBeanDefinitions(DefaultListableBeanFactory)}
     * phase.
     *
     * @param source any object that can register bean definitions on demand
     */
    public void registerBeanSource(BeanDefinitionSource source) {
        sources.add(source);
    }

    /**
     * Load bean definitions from the registered sources into the supplied registry/factor, in the order of their
     * registration.
     */
    @Override
    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
        sources.forEach(source -> source.registerBeans(beanFactory));
    }

    /**
     * Run any post-processors on the registry after loading definitions, but before starting.
     * <p>
     * Any annotation-driven post-processors will be picked up by the base context behavior. However, we also ensure
     * that any special Sakai beans are detected. Any class implementing {@link BeanFactoryPostProcessorCreator} will be
     * invoked to get additional post-processors before moving up to the built-in Spring behavior.
     *
     * @param beanFactory the bean factory used by the application context
     */
    @Override
    protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        dynamicBeanPostProcessors(beanFactory).forEach(this::addBeanFactoryPostProcessor);
        super.postProcessBeanFactory(beanFactory);
    }

    private Stream<BeanFactoryPostProcessor> dynamicBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
        return beanFactory.getBeansOfType(BeanFactoryPostProcessorCreator.class, false, false)
                .values()
                .stream()
                .flatMap(c -> c.getBeanFactoryPostProcessors().stream());
    }
}
