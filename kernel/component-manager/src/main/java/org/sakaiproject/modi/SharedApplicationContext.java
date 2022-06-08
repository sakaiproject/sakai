package org.sakaiproject.modi;

import org.sakaiproject.util.BeanFactoryPostProcessorCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.AbstractRefreshableApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * An ApplicationContext that can be shared across webapps as a parent. This serves as the main container for Sakai,
 * and the effective replacement for the ComponentManager. It loads beans indirectly, from sources that can yield
 * definitions. This inversion allows components (or overrides, or any source of beans) to be read or prepared in any
 * format, rather than requiring XML files in a certain layout.
 */
public class SharedApplicationContext extends AbstractRefreshableApplicationContext {

    protected final List<BeanDefinitionSource> sources = new ArrayList<>();

    public void registerBeanSource(BeanDefinitionSource source) {
        sources.add(source);
    }

    @Override
    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
        sources.forEach(source -> source.registerBeans(beanFactory));
    }

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
