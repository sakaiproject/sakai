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

    /** The ordered list of sources of bean definitions. */
    protected final List<BeanDefinitionSource> sources = new ArrayList<>();

    /**
     * Register a source of bean definitions with the context.
     *
     * The sources will be invoked during refresh, in the {@link #loadBeanDefinitions(DefaultListableBeanFactory)}
     * phase.
     *
     * @param source any object that can register bean definitions on demand
     */
    public void registerBeanSource(BeanDefinitionSource source) {
        sources.add(source);
    }

    /**
     * Load bean definitions from the registered sources, in the order of their registration.
     *
     * @param beanFactory the bean factory to load bean definitions into
     * @throws BeansException
     * @throws IOException
     */
    @Override
    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
        sources.forEach(source -> source.registerBeans(beanFactory));
    }

    /**
     * Run any post-processors on the registry after loading definitions, but before starting.
     *
     * Any annotation-driven post-processors will be picked up by the base context behavior.
     * However, we also ensure that any special Sakai beans are detected. Any class implementing
     * {@link BeanFactoryPostProcessorCreator} will be invoked to get additional post-processors
     * before moving up to the built-in Spring behavior.
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
