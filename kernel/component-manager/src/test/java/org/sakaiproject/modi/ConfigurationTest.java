package org.sakaiproject.modi;

import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * Test that the fundamental configuration loading is working.
 *
 * We don't want to test every detail of sakai-configuration.xml, but we do want to confirm that
 * the three main config/property beans are loaded and that we can supply custom configuration.
 * Everything else depends on these beans, so if something changes, we want to be aware.
 */
public class ConfigurationTest {
    @Test
    public void givenDefaultConfiguration_whenRegistering_thenDefaultSakaiPropertiesIsLoaded() {
        Configuration config = new Configuration();
        BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();

        config.registerBeans(registry);

        boolean loaded = registry.containsBeanDefinition("org.sakaiproject.component.DefaultSakaiProperties");
        assertThat(loaded).as("DefaultSakaiProperties bean is loaded").isTrue();
    }

    @Test
    public void givenDefaultConfiguration_whenRegistering_thenSakaiPropertiesIsLoaded() {
        Configuration config = new Configuration();
        BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();

        config.registerBeans(registry);

        boolean loaded = registry.containsBeanDefinition("org.sakaiproject.component.SakaiProperties");
        assertThat(loaded).as("SakaiProperties bean is loaded").isTrue();
    }

    @Test
    public void givenDefaultConfiguration_whenRegistering_thenPropertyPromoterIsLoaded() {
        Configuration config = new Configuration();
        BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();

        config.registerBeans(registry);

        boolean loaded = registry.containsBeanDefinition("org.sakaiproject.component.SakaiPropertyPromoter");
        assertThat(loaded).as("SakaiPropertyPromoter bean is loaded").isTrue();
    }

    @Test
    public void givenAMissingConfigFile_whenConstructing_thenExceptionIsThrown() throws IOException {
        Path tmpFile = Files.createTempFile("missing-config-", ".xml");
        Files.delete(tmpFile);

        Exception thrown = catchException(() -> new Configuration(tmpFile));

        assertThat(thrown).hasMessageContaining("missing supplied startup configuration");
    }

    @Test
    public void givenACustomSakaiProperties_whenRegistering_thenTheDefaultIsOverridden() throws MissingConfigurationException {
        Configuration config = new Configuration(Path.of("src/test/resources/override-properties/sakai-configuration.xml"));
        BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();

        config.registerBeans(registry);

        BeanDefinition bean = registry.getBeanDefinition("org.sakaiproject.component.SakaiProperties");
        String value = beanPropertyString(bean, "testprop");
        assertThat(value).isEqualTo("is set");
    }

    private String beanPropertyString(BeanDefinition bean, String property) {
        return ((TypedStringValue) bean.getPropertyValues().get(property)).getValue();
    }
}