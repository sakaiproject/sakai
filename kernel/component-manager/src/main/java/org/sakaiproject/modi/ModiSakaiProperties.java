package org.sakaiproject.modi;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.util.SakaiProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.PriorityOrdered;

import java.util.Collection;
import java.util.Collections;

/**
 * We now turn the SakaiProperties bean into first-class post-processor and delegate to the other two while running,
 * rather than relying on the old BeanFactoryPostProcessorCreator model that required a custom context. If we wanted to
 * create/expose these programmatically, we could use the
 * {@link org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor} interface, which operates
 * beforehand; giving us a standard lifecycle hook for what our custom interface did.
 * <p>
 * // TODO: Unwind the complicated relationship between BasicConfigurationService and SakaiProperties.
 *          There are multiple issues stemming from where the properties are loaded/held, and where they are used in
 *          merged, source-specific, raw, or expanded form. This is precluding a straightforward replacement of this
 *          class with a standard PropertySourcesPlaceholderConfigurer and related.
 * <p>
 * The startup process for a monolithic modi boot (kernel + components) should be:
 * <ol>
 *     <li>Add the base configuration (sakai-configuration.xml) to the context</li>
 *     <li>Add the modi configuration (modi-configuration.xml) to change the default bean class to this one</li>
 *     <li>Set kernel.properties (from classpath, always literal path and literal values only) in default bean</li>
 *     <li>Set sakai.properties and the others in the overridable bean, with the list set to merge</li>
 *     <li>Run a placeholder pass with no sources, system properties in properties file paths (bean values)</li>
 *     <li>Load the entire stack of properties files in one pass</li>
 *     <li>Run an override/qualifier pass for targeting properties to specific beans</li>
 *     <li>Run a placeholder pass for replacing placeholders in bean values</li>
 *     <li>Promote any special property values to system properties</li>
 *     <li>Proceed with context refresh, loading kernel and components, and start</li>
 * </ol>
 * A setup where the kernel is in one context and the other components ("modules") are in a child context is not too
 * far off. It would require separating the bean definitions that should apply in the kernel context from those that
 * should apply elsewhere. As it stands, implementors can override anything, whether from the kernel or another
 * component with the single sakai-configuration.xml. There would typically be no, or very little, customization at
 * the kernel bean level, but the properties files must apply. Then, a separate bean definition file could apply in
 * the component/module layer, inheriting the properties loaded for the kernel. The properties targeted at each layer
 * could be separated, but would not be necessary to achieve isolation.
 */
@Slf4j
public class ModiSakaiProperties extends SakaiProperties implements BeanFactoryPostProcessor, PriorityOrdered {
    /** The order of when to run this post-processor; lower is sooner */
    @Getter
    @Setter
    int order;

    /**
     * The base class yields its override and placeholder processors for registration. We simply delegate to them in
     * {@link #postProcessBeanFactory(ConfigurableListableBeanFactory).
     *
     * @return an empty list, always
     */
    @Override
    public Collection<BeanFactoryPostProcessor> getBeanFactoryPostProcessors() {
        return Collections.emptyList();
    }

    /**
     * Delegate to our post-processors for overrides and placeholders.
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        propertyOverrideConfigurer.postProcessBeanFactory(beanFactory);
        propertyPlaceholderConfigurer.postProcessBeanFactory(beanFactory);
        log.debug("Processed property overrides (target bean properties) and placeholders (like ${auto.ddl}).");
    }
}
